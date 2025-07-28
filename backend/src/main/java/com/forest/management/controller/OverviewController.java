package com.forest.management.controller;

import com.forest.management.model.*;
import com.forest.management.service.DataGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统概览控制器
 */
@RestController
@RequestMapping("/api/overview")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class OverviewController {
    
    @Autowired
    private DataGeneratorService dataGeneratorService;
    
    /**
     * 获取系统概览统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOverviewStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取各模块数据
        Map<Long, TreeArchive> trees = dataGeneratorService.getTreeArchives();
        Map<Long, ForestLand> forestLands = dataGeneratorService.getForestLands();
        Map<Long, ResourceMonitor> monitors = dataGeneratorService.getResourceMonitors();
        Map<Long, ForestRights> rights = dataGeneratorService.getForestRights();
        Map<Long, CuttingPermit> permits = dataGeneratorService.getCuttingPermits();
        
        // 基础统计
        stats.put("totalTrees", trees.size());
        stats.put("totalForestLands", forestLands.size());
        stats.put("totalRights", rights.size());
        stats.put("totalPermits", permits.size());
        stats.put("totalMonitorRecords", monitors.size());
        
        // 林地总面积
        double totalArea = forestLands.values().stream()
            .mapToDouble(ForestLand::getArea)
            .sum();
        stats.put("totalArea", Math.round(totalArea * 100.0) / 100.0);
        
        // 林木健康状态统计
        Map<String, Long> healthStats = trees.values().stream()
            .collect(Collectors.groupingBy(TreeArchive::getHealthStatus, Collectors.counting()));
        stats.put("treeHealthStats", healthStats);
        
        // 林地分类统计
        Map<String, Long> classificationStats = forestLands.values().stream()
            .collect(Collectors.groupingBy(ForestLand::getClassification, Collectors.counting()));
        stats.put("forestClassificationStats", classificationStats);
        
        // 林权证书状态统计
        Map<String, Long> rightsStatusStats = rights.values().stream()
            .collect(Collectors.groupingBy(ForestRights::getStatus, Collectors.counting()));
        stats.put("rightsStatusStats", rightsStatusStats);
        
        // 采伐许可状态统计
        Map<String, Long> permitsStatusStats = permits.values().stream()
            .collect(Collectors.groupingBy(CuttingPermit::getStatus, Collectors.counting()));
        stats.put("permitsStatusStats", permitsStatusStats);
        
        // 即将到期的证书和许可数量
        long expiringRights = rights.values().stream()
            .filter(ForestRights::isExpiringSoon)
            .count();
        stats.put("expiringRights", expiringRights);
        
        long expiringPermits = permits.values().stream()
            .filter(CuttingPermit::isExpiringSoon)
            .count();
        stats.put("expiringPermits", expiringPermits);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取趋势分析数据
     */
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getTrendAnalysis(
            @RequestParam(defaultValue = "2024") String year) {
        
        Map<String, Object> trends = new HashMap<>();
        
        // 获取监测数据
        Map<Long, ResourceMonitor> monitors = dataGeneratorService.getResourceMonitors();
        List<ResourceMonitor> yearMonitors = monitors.values().stream()
            .filter(monitor -> monitor.getMonitorDate().getYear() == Integer.parseInt(year))
            .collect(Collectors.toList());
        
        // 按月份分组
        Map<String, List<ResourceMonitor>> monthlyData = yearMonitors.stream()
            .collect(Collectors.groupingBy(monitor -> 
                monitor.getMonitorDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));
        
        // 生成月度趋势数据
        List<Map<String, Object>> monthlyTrends = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            String monthKey = String.format("%s-%02d", year, month);
            List<ResourceMonitor> monthData = monthlyData.getOrDefault(monthKey, new ArrayList<>());
            
            Map<String, Object> monthTrend = new HashMap<>();
            monthTrend.put("month", monthKey);
            monthTrend.put("monthName", month + "月");
            
            // 计算平均生长量和蓄积量
            OptionalDouble avgGrowth = monthData.stream()
                .filter(m -> "生长量".equals(m.getMonitorType()))
                .mapToDouble(ResourceMonitor::getCurrentValue)
                .average();
            
            OptionalDouble avgVolume = monthData.stream()
                .filter(m -> "蓄积量".equals(m.getMonitorType()))
                .mapToDouble(ResourceMonitor::getCurrentValue)
                .average();
            
            monthTrend.put("avgGrowth", avgGrowth.isPresent() ? 
                Math.round(avgGrowth.getAsDouble() * 100.0) / 100.0 : 0);
            monthTrend.put("avgVolume", avgVolume.isPresent() ? 
                Math.round(avgVolume.getAsDouble() * 100.0) / 100.0 : 0);
            monthTrend.put("recordCount", monthData.size());
            
            monthlyTrends.add(monthTrend);
        }
        
        trends.put("monthlyTrends", monthlyTrends);
        trends.put("year", year);
        
        return ResponseEntity.ok(trends);
    }
    
    /**
     * 获取预警信息
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<Map<String, Object>>> getAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // 获取各模块数据
        Map<Long, ForestRights> rights = dataGeneratorService.getForestRights();
        Map<Long, CuttingPermit> permits = dataGeneratorService.getCuttingPermits();
        Map<Long, ResourceMonitor> monitors = dataGeneratorService.getResourceMonitors();
        
        // 林权证书到期预警
        rights.values().stream()
            .filter(ForestRights::isExpiringSoon)
            .forEach(right -> {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "rights_expiry");
                alert.put("level", right.isExpired() ? "danger" : "warning");
                alert.put("title", "林权证书即将到期");
                alert.put("message", String.format("证书 %s 将于 %s 到期", 
                    right.getCertificateNo(), right.getExpiryDate()));
                alert.put("data", right);
                alert.put("createTime", new Date());
                alerts.add(alert);
            });
        
        // 采伐许可到期预警
        permits.values().stream()
            .filter(CuttingPermit::isExpiringSoon)
            .forEach(permit -> {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "permit_expiry");
                alert.put("level", permit.isExpired() ? "danger" : "warning");
                alert.put("title", "采伐许可即将到期");
                alert.put("message", String.format("许可 %s 将于 %s 到期", 
                    permit.getPermitNo(), permit.getValidUntil()));
                alert.put("data", permit);
                alert.put("createTime", new Date());
                alerts.add(alert);
            });
        
        // 资源异常变化预警
        monitors.values().stream()
            .filter(monitor -> monitor.getChangeRate() != null && Math.abs(monitor.getChangeRate()) > 30)
            .limit(5) // 限制数量
            .forEach(monitor -> {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "resource_change");
                alert.put("level", Math.abs(monitor.getChangeRate()) > 50 ? "danger" : "warning");
                alert.put("title", "资源变化异常");
                alert.put("message", String.format("%s 的 %s 变化率达到 %.1f%%", 
                    monitor.getForestLandName(), monitor.getMonitorType(), monitor.getChangeRate()));
                alert.put("data", monitor);
                alert.put("createTime", new Date());
                alerts.add(alert);
            });
        
        // 按严重程度排序
        alerts.sort((a, b) -> {
            String levelA = (String) a.get("level");
            String levelB = (String) b.get("level");
            if ("danger".equals(levelA) && !"danger".equals(levelB)) return -1;
            if ("danger".equals(levelB) && !"danger".equals(levelA)) return 1;
            return 0;
        });
        
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * 获取最近活动
     */
    @GetMapping("/recent-activities")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // 获取各模块数据
        Map<Long, TreeArchive> trees = dataGeneratorService.getTreeArchives();
        Map<Long, ForestLand> forestLands = dataGeneratorService.getForestLands();
        Map<Long, ForestRights> rights = dataGeneratorService.getForestRights();
        Map<Long, CuttingPermit> permits = dataGeneratorService.getCuttingPermits();
        
        // 最近创建的林木档案
        trees.values().stream()
            .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
            .limit(3)
            .forEach(tree -> {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "tree_created");
                activity.put("icon", "Document");
                activity.put("title", "新增林木档案");
                activity.put("description", String.format("新增了 %s 的档案记录", tree.getTreeSpecies()));
                activity.put("time", tree.getCreateTime());
                activities.add(activity);
            });
        
        // 最近创建的林地
        forestLands.values().stream()
            .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
            .limit(2)
            .forEach(land -> {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "forestland_created");
                activity.put("icon", "MapLocation");
                activity.put("title", "新增林地信息");
                activity.put("description", String.format("新增了 %s (%s)", land.getName(), land.getClassification()));
                activity.put("time", land.getCreateTime());
                activities.add(activity);
            });
        
        // 最近的证书
        rights.values().stream()
            .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
            .limit(2)
            .forEach(right -> {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "rights_created");
                activity.put("icon", "Notebook");
                activity.put("title", "新增林权证书");
                activity.put("description", String.format("为 %s 颁发了证书 %s", 
                    right.getOwnerName(), right.getCertificateNo()));
                activity.put("time", right.getCreateTime());
                activities.add(activity);
            });
        
        // 最近的许可审批
        permits.values().stream()
            .filter(permit -> !"待审批".equals(permit.getStatus()))
            .sorted((a, b) -> {
                LocalDate dateA = a.getApprovalDate() != null ? a.getApprovalDate() : a.getApplicationDate();
                LocalDate dateB = b.getApprovalDate() != null ? b.getApprovalDate() : b.getApplicationDate();
                return dateB.compareTo(dateA);
            })
            .limit(2)
            .forEach(permit -> {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", "permit_approved");
                activity.put("icon", "EditPen");
                activity.put("title", "采伐许可审批");
                activity.put("description", String.format("许可 %s 已%s", 
                    permit.getPermitNo(), permit.getStatus()));
                activity.put("time", permit.getApprovalDate() != null ? 
                    permit.getApprovalDate().atStartOfDay() : permit.getCreateTime());
                activities.add(activity);
            });
        
        // 按时间排序
        activities.sort((a, b) -> {
            Object timeA = a.get("time");
            Object timeB = b.get("time");
            if (timeA instanceof java.time.LocalDateTime && timeB instanceof java.time.LocalDateTime) {
                return ((java.time.LocalDateTime) timeB).compareTo((java.time.LocalDateTime) timeA);
            }
            return 0;
        });
        
        return ResponseEntity.ok(activities.stream().limit(10).collect(Collectors.toList()));
    }
    
    /**
     * 获取系统运行状态
     */
    @GetMapping("/system-status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 系统基本信息
        status.put("version", "v1.0.0");
        status.put("buildTime", "2024-01-15");
        status.put("runMode", "演示模式");
        status.put("dataStorage", "内存存储");
        status.put("startTime", new Date());
        
        // 数据统计
        status.put("totalRecords", 
            dataGeneratorService.getTreeArchives().size() +
            dataGeneratorService.getForestLands().size() +
            dataGeneratorService.getResourceMonitors().size() +
            dataGeneratorService.getForestRights().size() +
            dataGeneratorService.getCuttingPermits().size());
        
        // 系统状态
        status.put("status", "运行正常");
        status.put("uptime", "持续运行");
        status.put("memoryUsage", "正常");
        status.put("responseTime", "< 100ms");
        
        return ResponseEntity.ok(status);
    }
}