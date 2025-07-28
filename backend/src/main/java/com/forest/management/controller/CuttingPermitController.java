package com.forest.management.controller;

import com.forest.management.model.CuttingPermit;
import com.forest.management.model.ForestLand;
import com.forest.management.service.DataGeneratorService;
import com.forest.management.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采伐许可管理控制器
 */
@RestController
@RequestMapping("/api/permits")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class CuttingPermitController {
    
    @Autowired
    private DataGeneratorService dataGeneratorService;
    
    @Autowired
    private ValidationService validationService;
    
    /**
     * 获取采伐许可列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPermitList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String permitNo,
            @RequestParam(required = false) String applicantName,
            @RequestParam(required = false) String status) {
        
        Map<Long, CuttingPermit> allPermits = dataGeneratorService.getCuttingPermits();
        List<CuttingPermit> permitList = new ArrayList<>(allPermits.values());
        
        // 过滤条件
        if (permitNo != null && !permitNo.trim().isEmpty()) {
            permitList = permitList.stream()
                .filter(permit -> permit.getPermitNo().contains(permitNo.trim()))
                .collect(Collectors.toList());
        }
        
        if (applicantName != null && !applicantName.trim().isEmpty()) {
            permitList = permitList.stream()
                .filter(permit -> permit.getApplicantName().contains(applicantName.trim()))
                .collect(Collectors.toList());
        }
        
        if (status != null && !status.trim().isEmpty()) {
            permitList = permitList.stream()
                .filter(permit -> permit.getStatus().equals(status.trim()))
                .collect(Collectors.toList());
        }
        
        // 排序
        permitList.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        
        // 分页
        int total = permitList.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        
        List<CuttingPermit> pagedList = start < total ? permitList.subList(start, end) : new ArrayList<>();
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", pagedList);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) total / size));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取许可详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CuttingPermit> getPermitById(@PathVariable Long id) {
        Map<Long, CuttingPermit> allPermits = dataGeneratorService.getCuttingPermits();
        CuttingPermit permit = allPermits.get(id);
        
        if (permit == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(permit);
    }
    
    /**
     * 新增采伐许可申请
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPermit(@Valid @RequestBody CuttingPermit permit) {
        Map<String, Object> response = new HashMap<>();
        
        // 数据验证
        List<String> errors = validatePermitData(permit);
        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("message", "数据验证失败");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 检查许可证编号是否重复
        boolean exists = dataGeneratorService.getCuttingPermits().values().stream()
            .anyMatch(existing -> existing.getPermitNo().equals(permit.getPermitNo()));
        
        if (exists) {
            response.put("success", false);
            response.put("message", "许可证编号已存在");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 设置关联的林地名称
        Map<Long, ForestLand> forestLands = dataGeneratorService.getForestLands();
        ForestLand forestLand = forestLands.get(permit.getForestLandId());
        if (forestLand != null) {
            permit.setForestLandName(forestLand.getName());
        }
        
        // 设置ID和时间
        permit.setId(dataGeneratorService.generatePermitId());
        permit.setCreateTime(LocalDateTime.now());
        permit.setUpdateTime(LocalDateTime.now());
        
        // 设置默认状态和申请日期
        permit.setStatus("待审批");
        if (permit.getApplicationDate() == null) {
            permit.setApplicationDate(LocalDate.now());
        }
        
        // 保存到内存
        dataGeneratorService.getCuttingPermits().put(permit.getId(), permit);
        
        response.put("success", true);
        response.put("message", "采伐许可申请提交成功");
        response.put("data", permit);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新采伐许可
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePermit(@PathVariable Long id, 
                                                           @Valid @RequestBody CuttingPermit permit) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, CuttingPermit> allPermits = dataGeneratorService.getCuttingPermits();
        
        CuttingPermit existingPermit = allPermits.get(id);
        if (existingPermit == null) {
            response.put("success", false);
            response.put("message", "采伐许可不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        // 数据验证
        List<String> errors = validatePermitData(permit);
        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("message", "数据验证失败");
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 检查许可证编号是否重复（排除自己）
        boolean exists = allPermits.values().stream()
            .anyMatch(existing -> !existing.getId().equals(id) && 
                     existing.getPermitNo().equals(permit.getPermitNo()));
        
        if (exists) {
            response.put("success", false);
            response.put("message", "许可证编号已存在");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 设置关联的林地名称
        Map<Long, ForestLand> forestLands = dataGeneratorService.getForestLands();
        ForestLand forestLand = forestLands.get(permit.getForestLandId());
        if (forestLand != null) {
            permit.setForestLandName(forestLand.getName());
        }
        
        // 更新数据
        permit.setId(id);
        permit.setCreateTime(existingPermit.getCreateTime());
        permit.setUpdateTime(LocalDateTime.now());
        
        // 保持原有的审批信息（如果是编辑申请信息）
        if ("待审批".equals(permit.getStatus())) {
            permit.setApprover(null);
            permit.setApprovalOpinion(null);
            permit.setApprovalDate(null);
            permit.setValidUntil(null);
        }
        
        allPermits.put(id, permit);
        
        response.put("success", true);
        response.put("message", "采伐许可更新成功");
        response.put("data", permit);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 审批采伐许可
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approvePermit(@PathVariable Long id, 
                                                            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, CuttingPermit> allPermits = dataGeneratorService.getCuttingPermits();
        
        CuttingPermit permit = allPermits.get(id);
        if (permit == null) {
            response.put("success", false);
            response.put("message", "采伐许可不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        String action = request.get("action"); // "approve" 或 "reject"
        String approver = request.get("approver");
        String opinion = request.get("opinion");
        
        if (!"待审批".equals(permit.getStatus())) {
            response.put("success", false);
            response.put("message", "该许可已经审批过了");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (approver == null || approver.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "审批人不能为空");
            return ResponseEntity.badRequest().body(response);
        }
        
        if ("approve".equals(action)) {
            permit.approve(approver, opinion);
            response.put("message", "采伐许可审批通过");
        } else if ("reject".equals(action)) {
            permit.reject(approver, opinion);
            response.put("message", "采伐许可审批拒绝");
        } else {
            response.put("success", false);
            response.put("message", "无效的审批操作");
            return ResponseEntity.badRequest().body(response);
        }
        
        response.put("success", true);
        response.put("data", permit);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除采伐许可
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePermit(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, CuttingPermit> allPermits = dataGeneratorService.getCuttingPermits();
        
        CuttingPermit permit = allPermits.remove(id);
        if (permit == null) {
            response.put("success", false);
            response.put("message", "采伐许可不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        response.put("success", true);
        response.put("message", "采伐许可删除成功");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 批量删除采伐许可
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> deletePermits(@RequestBody List<Long> ids) {
        Map<String, Object> response = new HashMap<>();
        Map<Long, CuttingPermit> allPermits = dataGeneratorService.getCuttingPermits();
        
        int deletedCount = 0;
        for (Long id : ids) {
            if (allPermits.remove(id) != null) {
                deletedCount++;
            }
        }
        
        response.put("success", true);
        response.put("message", "成功删除 " + deletedCount + " 条采伐许可");
        response.put("deletedCount", deletedCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取采伐统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String groupBy) {
        
        Map<Long, CuttingPermit> allPermits = dataGeneratorService.getCuttingPermits();
        List<CuttingPermit> permitList = new ArrayList<>(allPermits.values());
        
        // 日期过滤
        if (startDate != null && !startDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            permitList = permitList.stream()
                .filter(permit -> !permit.getApplicationDate().isBefore(start))
                .collect(Collectors.toList());
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            LocalDate end = LocalDate.parse(endDate);
            permitList = permitList.stream()
                .filter(permit -> !permit.getApplicationDate().isAfter(end))
                .collect(Collectors.toList());
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        // 基础统计
        stats.put("totalCount", permitList.size());
        
        // 按状态统计
        Map<String, Long> statusStats = permitList.stream()
            .collect(Collectors.groupingBy(CuttingPermit::getStatus, Collectors.counting()));
        stats.put("statusStats", statusStats);
        
        // 总采伐面积和采伐量
        double totalArea = permitList.stream()
            .filter(permit -> "已批准".equals(permit.getStatus()))
            .mapToDouble(CuttingPermit::getCuttingArea)
            .sum();
        
        double totalVolume = permitList.stream()
            .filter(permit -> "已批准".equals(permit.getStatus()))
            .mapToDouble(CuttingPermit::getCuttingVolume)
            .sum();
        
        stats.put("totalCuttingArea", Math.round(totalArea * 100.0) / 100.0);
        stats.put("totalCuttingVolume", Math.round(totalVolume * 100.0) / 100.0);
        
        // 按时间分组统计
        if ("month".equals(groupBy)) {
            Map<String, List<CuttingPermit>> monthlyData = permitList.stream()
                .collect(Collectors.groupingBy(permit -> 
                    permit.getApplicationDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));
            
            List<Map<String, Object>> monthlyStats = new ArrayList<>();
            for (Map.Entry<String, List<CuttingPermit>> entry : monthlyData.entrySet()) {
                Map<String, Object> monthStat = new HashMap<>();
                monthStat.put("month", entry.getKey());
                monthStat.put("count", entry.getValue().size());
                
                double monthArea = entry.getValue().stream()
                    .filter(permit -> "已批准".equals(permit.getStatus()))
                    .mapToDouble(CuttingPermit::getCuttingArea)
                    .sum();
                
                double monthVolume = entry.getValue().stream()
                    .filter(permit -> "已批准".equals(permit.getStatus()))
                    .mapToDouble(CuttingPermit::getCuttingVolume)
                    .sum();
                
                monthStat.put("area", Math.round(monthArea * 100.0) / 100.0);
                monthStat.put("volume", Math.round(monthVolume * 100.0) / 100.0);
                
                monthlyStats.add(monthStat);
            }
            
            // 按月份排序
            monthlyStats.sort((a, b) -> ((String) a.get("month")).compareTo((String) b.get("month")));
            stats.put("monthlyStats", monthlyStats);
        }
        
        // 即将到期的许可数量
        long expiringSoonCount = permitList.stream()
            .filter(CuttingPermit::isExpiringSoon)
            .count();
        stats.put("expiringSoonCount", expiringSoonCount);
        
        // 已过期的许可数量
        long expiredCount = permitList.stream()
            .filter(CuttingPermit::isExpired)
            .count();
        stats.put("expiredCount", expiredCount);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取待审批的许可列表
     */
    @GetMapping("/pending")
    public ResponseEntity<List<CuttingPermit>> getPendingPermits() {
        Map<Long, CuttingPermit> allPermits = dataGeneratorService.getCuttingPermits();
        
        List<CuttingPermit> pendingPermits = allPermits.values().stream()
            .filter(permit -> "待审批".equals(permit.getStatus()))
            .sorted(Comparator.comparing(CuttingPermit::getApplicationDate))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(pendingPermits);
    }
    
    /**
     * 获取即将到期的许可列表
     */
    @GetMapping("/expiring")
    public ResponseEntity<List<CuttingPermit>> getExpiringPermits() {
        Map<Long, CuttingPermit> allPermits = dataGeneratorService.getCuttingPermits();
        
        List<CuttingPermit> expiringPermits = allPermits.values().stream()
            .filter(CuttingPermit::isExpiringSoon)
            .sorted(Comparator.comparing(CuttingPermit::getValidUntil))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(expiringPermits);
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
     * 验证采伐许可数据
     */
    private List<String> validatePermitData(CuttingPermit permit) {
        List<String> errors = new ArrayList<>();
        
        if (!validationService.isValidCertificateNo(permit.getPermitNo())) {
            errors.add("许可证编号格式不正确");
        }
        
        if (!validationService.isValidName(permit.getApplicantName())) {
            errors.add("申请人姓名格式不正确");
        }
        
        if (permit.getApplicantIdCard() != null && !permit.getApplicantIdCard().isEmpty() && 
            !validationService.isValidIdCard(permit.getApplicantIdCard())) {
            errors.add("身份证号格式不正确");
        }
        
        if (permit.getApplicantPhone() != null && !permit.getApplicantPhone().isEmpty() && 
            !validationService.isValidPhone(permit.getApplicantPhone())) {
            errors.add("手机号格式不正确");
        }
        
        if (permit.getForestLandId() == null) {
            errors.add("必须选择采伐地点");
        } else {
            // 检查林地是否存在
            Map<Long, ForestLand> forestLands = dataGeneratorService.getForestLands();
            if (!forestLands.containsKey(permit.getForestLandId())) {
                errors.add("选择的林地不存在");
            }
        }
        
        if (!validationService.isValidArea(permit.getCuttingArea())) {
            errors.add("采伐面积必须在0.1-10000公顷之间");
        }
        
        if (!validationService.isValidCuttingVolume(permit.getCuttingVolume())) {
            errors.add("采伐量必须在1-10000立方米之间");
        }
        
        if (permit.getReason() == null || permit.getReason().trim().isEmpty()) {
            errors.add("申请理由不能为空");
        } else if (!validationService.isValidLength(permit.getReason(), 5, 500)) {
            errors.add("申请理由长度必须在5-500个字符之间");
        }
        
        return errors;
    }
}