package com.forest.management.controller;

import com.forest.management.model.ForestRights;
import com.forest.management.model.ForestLand;
import com.forest.management.service.DataGeneratorService;
import com.forest.management.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 林权证书管理控制器
 */
@RestController
@RequestMapping("/api/rights")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ForestRightsController {
    
    @Autowired
    private DataGeneratorService dataGeneratorService;
    
    @Autowired
    private ValidationService validationService;
    
    /**
     * 获取林权证书列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getRightsList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String certificateNo,
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) String status) {
        
        Map<Long, ForestRights> allRights = dataGeneratorService.getForestRights();
        List<ForestRights> rightsList = new ArrayList<>(allRights.values());
        
        // 过滤条件
        if (certificateNo != null && !certificateNo.trim().isEmpty()) {
            rightsList = rightsList.stream()
                .filter(rights -> rights.getCertificateNo().contains(certificateNo.trim()))
                .collect(Collectors.toList());
        }
        
        if (ownerName != null && !ownerName.trim().isEmpty()) {
            rightsList = rightsList.stream()
                .filter(rights -> rights.getOwnerName().contains(ownerName.trim()))
                .collect(Collectors.toList());
        }
        
        if (status != null && !status.trim().isEmpty()) {
            rightsList = rightsList.stream()
                .filter(rights -> rights.getStatus().equals(status.trim()))
                .collect(Collectors.toList());
        }
        
        // 排序
        rightsList.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        
        // 分页
        int total = rightsList.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        
        List<ForestRights> pagedList = start < total ? rightsList.subList(start, end) : new ArrayList<>();
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", pagedList);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) total / size));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取即将到期的证书
     */
    @GetMapping("/expiring")
    public ResponseEntity<List<ForestRights>> getExpiringRights() {
        Map<Long, ForestRights> allRights = dataGeneratorService.getForestRights();
        
        List<ForestRights> expiringRights = allRights.values().stream()
            .filter(ForestRights::isExpiringSoon)
            .sorted(Comparator.comparing(ForestRights::getExpiryDate))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(expiringRights);
    }
    
    /**
     * 获取证书详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ForestRights> getRightsById(@PathVariable Long id) {
        Map<Long, ForestRights> allRights = dataGeneratorService.getForestRights();
        ForestRights rights = allRights.get(id);
        
        if (rights == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(rights);
    }
    
    /**
     * 新增林权证书
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRights(@Valid @RequestBody ForestRights rights) {
        Map<String, Object> response = new HashMap<>();
        
        // 数据验证
        List<String> errors = validateRightsData(rights);
        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("message", "数据验证失败");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 检查证书编号是否重复
        boolean exists = dataGeneratorService.getForestRights().values().stream()
            .anyMatch(existing -> existing.getCertificateNo().equals(rights.getCertificateNo()));
        
        if (exists) {
            response.put("success", false);
            response.put("message", "证书编号已存在");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 设置关联的林地名称
        Map<Long, ForestLand> forestLands = dataGeneratorService.getForestLands();
        ForestLand forestLand = forestLands.get(rights.getForestLandId());
        if (forestLand != null) {
            rights.setForestLandName(forestLand.getName());
        }
        
        // 设置ID和时间
        rights.setId(dataGeneratorService.generateRightsId());
        rights.setCreateTime(LocalDateTime.now());
        rights.setUpdateTime(LocalDateTime.now());
        
        // 自动更新状态
        updateRightsStatus(rights);
        
        // 保存到内存
        dataGeneratorService.getForestRights().put(rights.getId(), rights);
        
        response.put("success", true);
        response.put("message", "林权证书创建成功");
        response.put("data", rights);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新林权证书
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRights(@PathVariable Long id, 
                                                           @Valid @RequestBody ForestRights rights) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, ForestRights> allRights = dataGeneratorService.getForestRights();
        
        ForestRights existingRights = allRights.get(id);
        if (existingRights == null) {
            response.put("success", false);
            response.put("message", "林权证书不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        // 数据验证
        List<String> errors = validateRightsData(rights);
        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("message", "数据验证失败");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 检查证书编号是否重复（排除自己）
        boolean exists = allRights.values().stream()
            .anyMatch(existing -> !existing.getId().equals(id) && 
                     existing.getCertificateNo().equals(rights.getCertificateNo()));
        
        if (exists) {
            response.put("success", false);
            response.put("message", "证书编号已存在");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 设置关联的林地名称
        Map<Long, ForestLand> forestLands = dataGeneratorService.getForestLands();
        ForestLand forestLand = forestLands.get(rights.getForestLandId());
        if (forestLand != null) {
            rights.setForestLandName(forestLand.getName());
        }
        
        // 更新数据
        rights.setId(id);
        rights.setCreateTime(existingRights.getCreateTime());
        rights.setUpdateTime(LocalDateTime.now());
        
        // 自动更新状态
        updateRightsStatus(rights);
        
        allRights.put(id, rights);
        
        response.put("success", true);
        response.put("message", "林权证书更新成功");
        response.put("data", rights);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新证书状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateRightsStatus(@PathVariable Long id, 
                                                                 @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, ForestRights> allRights = dataGeneratorService.getForestRights();
        
        ForestRights rights = allRights.get(id);
        if (rights == null) {
            response.put("success", false);
            response.put("message", "林权证书不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        String newStatus = request.get("status");
        if (!validationService.isValidRightsStatus(newStatus)) {
            response.put("success", false);
            response.put("message", "无效的证书状态");
            return ResponseEntity.badRequest().body(response);
        }
        
        rights.setStatus(newStatus);
        rights.setUpdateTime(LocalDateTime.now());
        
        response.put("success", true);
        response.put("message", "证书状态更新成功");
        response.put("data", rights);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除林权证书
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRights(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, ForestRights> allRights = dataGeneratorService.getForestRights();
        
        ForestRights rights = allRights.remove(id);
        if (rights == null) {
            response.put("success", false);
            response.put("message", "林权证书不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        response.put("success", true);
        response.put("message", "林权证书删除成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 批量删除林权证书
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> deleteRights(@RequestBody List<Long> ids) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, ForestRights> allRights = dataGeneratorService.getForestRights();
        
        int deletedCount = 0;
        for (Long id : ids) {
            if (allRights.remove(id) != null) {
                deletedCount++;
            }
        }
        
        response.put("success", true);
        response.put("message", "成功删除 " + deletedCount + " 条林权证书");
        response.put("deletedCount", deletedCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<Long, ForestRights> allRights = dataGeneratorService.getForestRights();
        List<ForestRights> rightsList = new ArrayList<>(allRights.values());
        
        Map<String, Object> stats = new HashMap<>();
        
        // 总数统计
        stats.put("totalCount", rightsList.size());
        
        // 按状态统计
        Map<String, Long> statusStats = rightsList.stream()
            .collect(Collectors.groupingBy(ForestRights::getStatus, Collectors.counting()));
        stats.put("statusStats", statusStats);
        
        // 即将到期数量
        long expiringSoonCount = rightsList.stream()
            .filter(ForestRights::isExpiringSoon)
            .count();
        stats.put("expiringSoonCount", expiringSoonCount);
        
        // 已过期数量
        long expiredCount = rightsList.stream()
            .filter(ForestRights::isExpired)
            .count();
        stats.put("expiredCount", expiredCount);
        
        // 按发证机关统计
        Map<String, Long> organStats = rightsList.stream()
            .filter(rights -> rights.getIssueOrgan() != null)
            .collect(Collectors.groupingBy(ForestRights::getIssueOrgan, Collectors.counting()));
        stats.put("organStats", organStats);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取林地选项（用于下拉选择）
     */
    @GetMapping("/forest-lands")
    public ResponseEntity<List<Map<String, Object>>> getForestLandOptions() {
        Map<Long, ForestLand> allForestLands = dataGeneratorService.getForestLands();
        
        List<Map<String, Object>> options = allForestLands.values().stream()
            .map(forestLand -> {
                Map<String, Object> option = new HashMap<>();
                option.put("id", forestLand.getId());
                option.put("name", forestLand.getName());
                option.put("classification", forestLand.getClassification());
                option.put("area", forestLand.getArea());
                return option;
            })
            .sorted((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(options);
    }
    
    /**
     * 验证林权证书数据
     */
    private List<String> validateRightsData(ForestRights rights) {
        List<String> errors = new ArrayList<>();
        
        if (!validationService.isValidCertificateNo(rights.getCertificateNo())) {
            errors.add("证书编号格式不正确");
        }
        
        if (!validationService.isValidName(rights.getOwnerName())) {
            errors.add("权利人姓名格式不正确");
        }
        
        if (rights.getOwnerIdCard() != null && !rights.getOwnerIdCard().isEmpty() && 
            !validationService.isValidIdCard(rights.getOwnerIdCard())) {
            errors.add("身份证号格式不正确");
        }
        
        if (rights.getOwnerPhone() != null && !rights.getOwnerPhone().isEmpty() && 
            !validationService.isValidPhone(rights.getOwnerPhone())) {
            errors.add("手机号格式不正确");
        }
        
        if (rights.getForestLandId() == null) {
            errors.add("必须选择关联的林地");
        } else {
            // 检查林地是否存在
            Map<Long, ForestLand> forestLands = dataGeneratorService.getForestLands();
            if (!forestLands.containsKey(rights.getForestLandId())) {
                errors.add("关联的林地不存在");
            }
        }
        
        if (rights.getIssueDate() == null) {
            errors.add("发证日期不能为空");
        }
        
        if (rights.getExpiryDate() == null) {
            errors.add("到期日期不能为空");
        }
        
        if (rights.getIssueDate() != null && rights.getExpiryDate() != null && 
            rights.getExpiryDate().isBefore(rights.getIssueDate())) {
            errors.add("到期日期不能早于发证日期");
        }
        
        return errors;
    }
    
    /**
     * 自动更新证书状态
     */
    private void updateRightsStatus(ForestRights rights) {
        if (rights.isExpired()) {
            rights.setStatus("过期");
        } else if (rights.isExpiringSoon()) {
            rights.setStatus("即将到期");
        } else if (!"注销".equals(rights.getStatus())) {
            rights.setStatus("有效");
        }
    }
}