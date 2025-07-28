package com.forest.management.controller;

import com.forest.management.model.ForestLand;
import com.forest.management.service.DataGeneratorService;
import com.forest.management.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 林地分类管理控制器
 */
@RestController
@RequestMapping("/api/forestlands")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ForestClassifyController {
    
    @Autowired
    private DataGeneratorService dataGeneratorService;
    
    @Autowired
    private ValidationService validationService;
    
    /**
     * 获取林地列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getForestLandList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String classification,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location) {
        
        Map<Long, ForestLand> allForestLands = dataGeneratorService.getForestLands();
        List<ForestLand> forestLandList = new ArrayList<>(allForestLands.values());
        
        // 过滤条件
        if (classification != null && !classification.trim().isEmpty()) {
            forestLandList = forestLandList.stream()
                .filter(land -> land.getClassification().equals(classification.trim()))
                .collect(Collectors.toList());
        }
        
        if (name != null && !name.trim().isEmpty()) {
            forestLandList = forestLandList.stream()
                .filter(land -> land.getName().contains(name.trim()))
                .collect(Collectors.toList());
        }
        
        if (location != null && !location.trim().isEmpty()) {
            forestLandList = forestLandList.stream()
                .filter(land -> land.getLocation() != null && land.getLocation().contains(location.trim()))
                .collect(Collectors.toList());
        }
        
        // 排序
        forestLandList.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        
        // 分页
        int total = forestLandList.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        
        List<ForestLand> pagedList = start < total ? forestLandList.subList(start, end) : new ArrayList<>();
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", pagedList);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) total / size));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取林地统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getForestLandStats() {
        Map<Long, ForestLand> allForestLands = dataGeneratorService.getForestLands();
        List<ForestLand> forestLandList = new ArrayList<>(allForestLands.values());
        
        Map<String, Object> stats = new HashMap<>();
        
        // 总数统计
        stats.put("totalCount", forestLandList.size());
        
        // 按分类统计数量
        Map<String, Long> classificationCounts = forestLandList.stream()
            .collect(Collectors.groupingBy(ForestLand::getClassification, Collectors.counting()));
        stats.put("classificationCounts", classificationCounts);
        
        // 按分类统计面积
        Map<String, Double> classificationAreas = new HashMap<>();
        for (String classification : Arrays.asList("用材林", "防护林", "经济林")) {
            double totalArea = forestLandList.stream()
                .filter(land -> classification.equals(land.getClassification()))
                .mapToDouble(ForestLand::getArea)
                .sum();
            classificationAreas.put(classification, Math.round(totalArea * 100.0) / 100.0);
        }
        stats.put("classificationAreas", classificationAreas);
        
        // 总面积
        double totalArea = forestLandList.stream()
            .mapToDouble(ForestLand::getArea)
            .sum();
        stats.put("totalArea", Math.round(totalArea * 100.0) / 100.0);
        
        // 平均面积
        OptionalDouble avgArea = forestLandList.stream()
            .mapToDouble(ForestLand::getArea)
            .average();
        stats.put("avgArea", avgArea.isPresent() ? Math.round(avgArea.getAsDouble() * 100.0) / 100.0 : 0);
        
        // 按位置统计
        Map<String, Long> locationStats = forestLandList.stream()
            .collect(Collectors.groupingBy(ForestLand::getLocation, Collectors.counting()));
        stats.put("locationStats", locationStats);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取林地详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ForestLand> getForestLandById(@PathVariable Long id) {
        Map<Long, ForestLand> allForestLands = dataGeneratorService.getForestLands();
        ForestLand forestLand = allForestLands.get(id);
        
        if (forestLand == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(forestLand);
    }
    
    /**
     * 新增林地信息
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createForestLand(@Valid @RequestBody ForestLand forestLand) {
        Map<String, Object> response = new HashMap<>();
        
        // 数据验证
        List<String> errors = validateForestLandData(forestLand);
        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("message", "数据验证失败");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 设置ID和时间
        forestLand.setId(dataGeneratorService.generateForestLandId());
        forestLand.setCreateTime(LocalDateTime.now());
        forestLand.setUpdateTime(LocalDateTime.now());
        
        // 保存到内存
        dataGeneratorService.getForestLands().put(forestLand.getId(), forestLand);
        
        response.put("success", true);
        response.put("message", "林地信息创建成功");
        response.put("data", forestLand);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新林地信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateForestLand(@PathVariable Long id, 
                                                               @Valid @RequestBody ForestLand forestLand) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, ForestLand> allForestLands = dataGeneratorService.getForestLands();
        
        ForestLand existingForestLand = allForestLands.get(id);
        if (existingForestLand == null) {
            response.put("success", false);
            response.put("message", "林地信息不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        // 数据验证
        List<String> errors = validateForestLandData(forestLand);
        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("message", "数据验证失败");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 更新数据
        forestLand.setId(id);
        forestLand.setCreateTime(existingForestLand.getCreateTime());
        forestLand.setUpdateTime(LocalDateTime.now());
        
        allForestLands.put(id, forestLand);
        
        response.put("success", true);
        response.put("message", "林地信息更新成功");
        response.put("data", forestLand);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除林地信息
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteForestLand(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, ForestLand> allForestLands = dataGeneratorService.getForestLands();
        
        ForestLand forestLand = allForestLands.remove(id);
        if (forestLand == null) {
            response.put("success", false);
            response.put("message", "林地信息不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        response.put("success", true);
        response.put("message", "林地信息删除成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 批量删除林地信息
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> deleteForestLands(@RequestBody List<Long> ids) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, ForestLand> allForestLands = dataGeneratorService.getForestLands();
        
        int deletedCount = 0;
        for (Long id : ids) {
            if (allForestLands.remove(id) != null) {
                deletedCount++;
            }
        }
        
        response.put("success", true);
        response.put("message", "成功删除 " + deletedCount + " 条林地信息");
        response.put("deletedCount", deletedCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 按分类获取林地列表
     */
    @GetMapping("/by-classification/{classification}")
    public ResponseEntity<List<ForestLand>> getForestLandsByClassification(@PathVariable String classification) {
        Map<Long, ForestLand> allForestLands = dataGeneratorService.getForestLands();
        
        List<ForestLand> filteredList = allForestLands.values().stream()
            .filter(land -> classification.equals(land.getClassification()))
            .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(filteredList);
    }
    
    /**
     * 验证林地数据
     */
    private List<String> validateForestLandData(ForestLand forestLand) {
        List<String> errors = new ArrayList<>();
        
        if (forestLand.getName() == null || forestLand.getName().trim().isEmpty()) {
            errors.add("林地名称不能为空");
        } else if (!validationService.isValidLength(forestLand.getName(), 2, 50)) {
            errors.add("林地名称长度必须在2-50个字符之间");
        }
        
        if (!validationService.isValidForestClassification(forestLand.getClassification())) {
            errors.add("林地分类不正确，必须是：用材林、防护林、经济林之一");
        }
        
        if (!validationService.isValidArea(forestLand.getArea())) {
            errors.add("林地面积必须在0.1-10000公顷之间");
        }
        
        if (forestLand.getLocation() != null && !validationService.isValidLocation(forestLand.getLocation())) {
            errors.add("位置信息格式不正确");
        }
        
        return errors;
    }
}