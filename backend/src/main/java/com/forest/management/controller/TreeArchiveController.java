package com.forest.management.controller;

import com.forest.management.model.TreeArchive;
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
 * 林木资源档案控制器
 */
@RestController
@RequestMapping("/api/trees")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class TreeArchiveController {
    
    @Autowired
    private DataGeneratorService dataGeneratorService;
    
    @Autowired
    private ValidationService validationService;
    
    /**
     * 获取林木列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTreeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String treeSpecies,
            @RequestParam(required = false) String healthStatus,
            @RequestParam(required = false) String location) {
        
        Map<Long, TreeArchive> allTrees = dataGeneratorService.getTreeArchives();
        List<TreeArchive> treeList = new ArrayList<>(allTrees.values());
        
        // 过滤条件
        if (treeSpecies != null && !treeSpecies.trim().isEmpty()) {
            treeList = treeList.stream()
                .filter(tree -> tree.getTreeSpecies().contains(treeSpecies.trim()))
                .collect(Collectors.toList());
        }
        
        if (healthStatus != null && !healthStatus.trim().isEmpty()) {
            treeList = treeList.stream()
                .filter(tree -> tree.getHealthStatus().equals(healthStatus.trim()))
                .collect(Collectors.toList());
        }
        
        if (location != null && !location.trim().isEmpty()) {
            treeList = treeList.stream()
                .filter(tree -> tree.getLocation() != null && tree.getLocation().contains(location.trim()))
                .collect(Collectors.toList());
        }
        
        // 排序
        treeList.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        
        // 分页
        int total = treeList.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        
        List<TreeArchive> pagedList = start < total ? treeList.subList(start, end) : new ArrayList<>();
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", pagedList);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) total / size));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取林木详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<TreeArchive> getTreeById(@PathVariable Long id) {
        Map<Long, TreeArchive> allTrees = dataGeneratorService.getTreeArchives();
        TreeArchive tree = allTrees.get(id);
        
        if (tree == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(tree);
    }
    
    /**
     * 新增林木档案
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTree(@Valid @RequestBody TreeArchive tree) {
        Map<String, Object> response = new HashMap<>();
        
        // 数据验证
        List<String> errors = validateTreeData(tree);
        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("message", "数据验证失败");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 设置ID和时间
        tree.setId(dataGeneratorService.generateTreeId());
        tree.setCreateTime(LocalDateTime.now());
        tree.setUpdateTime(LocalDateTime.now());
        
        // 保存到内存
        dataGeneratorService.getTreeArchives().put(tree.getId(), tree);
        
        response.put("success", true);
        response.put("message", "林木档案创建成功");
        response.put("data", tree);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新林木档案
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTree(@PathVariable Long id, 
                                                         @Valid @RequestBody TreeArchive tree) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, TreeArchive> allTrees = dataGeneratorService.getTreeArchives();
        
        TreeArchive existingTree = allTrees.get(id);
        if (existingTree == null) {
            response.put("success", false);
            response.put("message", "林木档案不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        // 数据验证
        List<String> errors = validateTreeData(tree);
        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("message", "数据验证失败");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 更新数据
        tree.setId(id);
        tree.setCreateTime(existingTree.getCreateTime());
        tree.setUpdateTime(LocalDateTime.now());
        
        allTrees.put(id, tree);
        
        response.put("success", true);
        response.put("message", "林木档案更新成功");
        response.put("data", tree);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除林木档案
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTree(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, TreeArchive> allTrees = dataGeneratorService.getTreeArchives();
        
        TreeArchive tree = allTrees.remove(id);
        if (tree == null) {
            response.put("success", false);
            response.put("message", "林木档案不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        response.put("success", true);
        response.put("message", "林木档案删除成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 批量删除林木档案
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> deleteTrees(@RequestBody List<Long> ids) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, TreeArchive> allTrees = dataGeneratorService.getTreeArchives();
        
        int deletedCount = 0;
        for (Long id : ids) {
            if (allTrees.remove(id) != null) {
                deletedCount++;
            }
        }
        
        response.put("success", true);
        response.put("message", "成功删除 " + deletedCount + " 条林木档案");
        response.put("deletedCount", deletedCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<Long, TreeArchive> allTrees = dataGeneratorService.getTreeArchives();
        List<TreeArchive> treeList = new ArrayList<>(allTrees.values());
        
        Map<String, Object> stats = new HashMap<>();
        
        // 总数统计
        stats.put("totalCount", treeList.size());
        
        // 按树种统计
        Map<String, Long> speciesStats = treeList.stream()
            .collect(Collectors.groupingBy(TreeArchive::getTreeSpecies, Collectors.counting()));
        stats.put("speciesStats", speciesStats);
        
        // 按健康状态统计
        Map<String, Long> healthStats = treeList.stream()
            .collect(Collectors.groupingBy(TreeArchive::getHealthStatus, Collectors.counting()));
        stats.put("healthStats", healthStats);
        
        // 平均胸径和树高
        OptionalDouble avgDiameter = treeList.stream().mapToDouble(TreeArchive::getDiameter).average();
        OptionalDouble avgHeight = treeList.stream().mapToDouble(TreeArchive::getHeight).average();
        
        stats.put("avgDiameter", avgDiameter.isPresent() ? Math.round(avgDiameter.getAsDouble() * 100.0) / 100.0 : 0);
        stats.put("avgHeight", avgHeight.isPresent() ? Math.round(avgHeight.getAsDouble() * 100.0) / 100.0 : 0);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 验证林木数据
     */
    private List<String> validateTreeData(TreeArchive tree) {
        List<String> errors = new ArrayList<>();
        
        if (!validationService.isValidTreeSpecies(tree.getTreeSpecies())) {
            errors.add("树种名称格式不正确");
        }
        
        if (!validationService.isValidDiameter(tree.getDiameter())) {
            errors.add("胸径必须在5-100cm之间");
        }
        
        if (!validationService.isValidHeight(tree.getHeight())) {
            errors.add("树高必须在1-50m之间");
        }
        
        if (!validationService.isValidHealthStatus(tree.getHealthStatus())) {
            errors.add("健康状态不正确");
        }
        
        if (tree.getLocation() != null && !validationService.isValidLocation(tree.getLocation())) {
            errors.add("位置信息格式不正确");
        }
        
        return errors;
    }
}