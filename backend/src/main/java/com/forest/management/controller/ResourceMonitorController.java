package com.forest.management.controller;

import com.forest.management.model.ResourceMonitor;
import com.forest.management.model.ForestLand;
import com.forest.management.service.DataGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 森林资源动态监测控制器
 */
@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ResourceMonitorController {
    
    @Autowired
    private DataGeneratorService dataGeneratorService;
    
    /**
     * 获取生长量数据
     */
    @GetMapping("/growth")
    public ResponseEntity<Map<String, Object>> getGrowthData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long forestLandId) {
        
        Map<Long, ResourceMonitor> allMonitors = dataGeneratorService.getResourceMonitors();
        List<ResourceMonitor> growthData = allMonitors.values().stream()
            .filter(monitor -> "生长量".equals(monitor.getMonitorType()))
            .collect(Collectors.toList());
        
        // 日期过滤
        if (startDate != null && !startDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            growthData = growthData.stream()
                .filter(monitor -> !monitor.getMonitorDate().isBefore(start))
                .collect(Collectors.toList());
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            LocalDate end = LocalDate.parse(endDate);
            growthData = growthData.stream()
                .filter(monitor -> !monitor.getMonitorDate().isAfter(end))
                .collect(Collectors.toList());
        }
        
        // 林地过滤
        if (forestLandId != null) {
            growthData = growthData.stream()
                .filter(monitor -> forestLandId.equals(monitor.getForestLandId()))
                .collect(Collectors.toList());
        }
        
        // 按日期排序
        growthData.sort(Comparator.comparing(ResourceMonitor::getMonitorDate));
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", growthData);
        response.put("total", growthData.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取蓄积量数据
     */
    @GetMapping("/volume")
    public ResponseEntity<Map<String, Object>> getVolumeData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long forestLandId) {
        
        Map<Long, ResourceMonitor> allMonitors = dataGeneratorService.getResourceMonitors();
        List<ResourceMonitor> volumeData = allMonitors.values().stream()
            .filter(monitor -> "蓄积量".equals(monitor.getMonitorType()))
            .collect(Collectors.toList());
        
        // 日期过滤
        if (startDate != null && !startDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            volumeData = volumeData.stream()
                .filter(monitor -> !monitor.getMonitorDate().isBefore(start))
                .collect(Collectors.toList());
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            LocalDate end = LocalDate.parse(endDate);
            volumeData = volumeData.stream()
                .filter(monitor -> !monitor.getMonitorDate().isAfter(end))
                .collect(Collectors.toList());
        }
        
        // 林地过滤
        if (forestLandId != null) {
            volumeData = volumeData.stream()
                .filter(monitor -> forestLandId.equals(monitor.getForestLandId()))
                .collect(Collectors.toList());
        }
        
        // 按日期排序
        volumeData.sort(Comparator.comparing(ResourceMonitor::getMonitorDate));
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", volumeData);
        response.put("total", volumeData.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取变化趋势数据
     */
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getTrends(
            @RequestParam(defaultValue = "2024") String year,
            @RequestParam(required = false) String monitorType) {
        
        Map<Long, ResourceMonitor> allMonitors = dataGeneratorService.getResourceMonitors();
        List<ResourceMonitor> monitorData = new ArrayList<>(allMonitors.values());
        
        // 年份过滤
        int targetYear = Integer.parseInt(year);
        monitorData = monitorData.stream()
            .filter(monitor -> monitor.getMonitorDate().getYear() == targetYear)
            .collect(Collectors.toList());
        
        // 监测类型过滤
        if (monitorType != null && !monitorType.isEmpty()) {
            monitorData = monitorData.stream()
                .filter(monitor -> monitorType.equals(monitor.getMonitorType()))
                .collect(Collectors.toList());
        }
        
        // 按月份分组统计
        Map<String, List<ResourceMonitor>> monthlyData = monitorData.stream()
            .collect(Collectors.groupingBy(monitor -> 
                monitor.getMonitorDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));
        
        // 构建趋势数据
        List<Map<String, Object>> trendData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            String monthKey = String.format("%d-%02d", targetYear, month);
            List<ResourceMonitor> monthData = monthlyData.getOrDefault(monthKey, new ArrayList<>());
            
            Map<String, Object> monthTrend = new HashMap<>();
            monthTrend.put("month", monthKey);
            monthTrend.put("monthName", month + "月");
            
            if (!monthData.isEmpty()) {
                // 按监测类型分组计算平均值
                Map<String, Double> typeAverages = monthData.stream()
                    .collect(Collectors.groupingBy(
                        ResourceMonitor::getMonitorType,
                        Collectors.averagingDouble(ResourceMonitor::getCurrentValue)
                    ));
                
                monthTrend.put("growthAvg", typeAverages.getOrDefault("生长量", 0.0));
                monthTrend.put("volumeAvg", typeAverages.getOrDefault("蓄积量", 0.0));
                monthTrend.put("dataCount", monthData.size());
            } else {
                monthTrend.put("growthAvg", 0.0);
                monthTrend.put("volumeAvg", 0.0);
                monthTrend.put("dataCount", 0);
            }
            
            trendData.add(monthTrend);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("trends", trendData);
        response.put("year", year);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取预警信息
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<Map<String, Object>>> getAlerts() {
        Map<Long, ResourceMonitor> allMonitors = dataGeneratorService.getResourceMonitors();
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // 获取最近的监测数据
        List<ResourceMonitor> recentData = allMonitors.values().stream()
            .filter(monitor -> monitor.getMonitorDate().isAfter(LocalDate.now().minusMonths(1)))
            .collect(Collectors.toList());
        
        // 检查异常变化
        for (ResourceMonitor monitor : recentData) {
            if (monitor.getChangeRate() != null) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", monitor.getId());
                alert.put("forestLandName", monitor.getForestLandName());
                alert.put("monitorType", monitor.getMonitorType());
                alert.put("changeRate", monitor.getChangeRate());
                alert.put("monitorDate", monitor.getMonitorDate());
                
                // 判断预警级别
                double changeRate = Math.abs(monitor.getChangeRate());
                if (changeRate > 50) {
                    alert.put("level", "严重");
                    alert.put("levelType", "danger");
                    alert.put("message", monitor.getMonitorType() + "变化异常，变化率达到" + 
                        String.format("%.1f", monitor.getChangeRate()) + "%");
                    alerts.add(alert);
                } else if (changeRate > 30) {
                    alert.put("level", "警告");
                    alert.put("levelType", "warning");
                    alert.put("message", monitor.getMonitorType() + "变化较大，变化率为" + 
                        String.format("%.1f", monitor.getChangeRate()) + "%");
                    alerts.add(alert);
                } else if (changeRate > 20) {
                    alert.put("level", "提醒");
                    alert.put("levelType", "info");
                    alert.put("message", monitor.getMonitorType() + "有所变化，变化率为" + 
                        String.format("%.1f", monitor.getChangeRate()) + "%");
                    alerts.add(alert);
                }
            }
        }
        
        // 按预警级别排序
        alerts.sort((a, b) -> {
            String levelA = (String) a.get("level");
            String levelB = (String) b.get("level");
            Map<String, Integer> levelOrder = Map.of("严重", 3, "警告", 2, "提醒", 1);
            return levelOrder.getOrDefault(levelB, 0) - levelOrder.getOrDefault(levelA, 0);
        });
        
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * 获取监测统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<Long, ResourceMonitor> allMonitors = dataGeneratorService.getResourceMonitors();
        Map<Long, ForestLand> allForestLands = dataGeneratorService.getForestLands();
        
        List<ResourceMonitor> monitorList = new ArrayList<>(allMonitors.values());
        
        Map<String, Object> stats = new HashMap<>();
        
        // 总监测点数
        stats.put("totalMonitorPoints", allForestLands.size());
        
        // 总监测记录数
        stats.put("totalRecords", monitorList.size());
        
        // 按监测类型统计
        Map<String, Long> typeStats = monitorList.stream()
            .collect(Collectors.groupingBy(ResourceMonitor::getMonitorType, Collectors.counting()));
        stats.put("typeStats", typeStats);
        
        // 最新监测数据
        Optional<ResourceMonitor> latestRecord = monitorList.stream()
            .max(Comparator.comparing(ResourceMonitor::getMonitorDate));
        stats.put("latestMonitorDate", latestRecord.map(ResourceMonitor::getMonitorDate).orElse(null));
        
        // 平均生长量和蓄积量
        OptionalDouble avgGrowth = monitorList.stream()
            .filter(monitor -> "生长量".equals(monitor.getMonitorType()))
            .mapToDouble(ResourceMonitor::getCurrentValue)
            .average();
        
        OptionalDouble avgVolume = monitorList.stream()
            .filter(monitor -> "蓄积量".equals(monitor.getMonitorType()))
            .mapToDouble(ResourceMonitor::getCurrentValue)
            .average();
        
        stats.put("avgGrowth", avgGrowth.isPresent() ? Math.round(avgGrowth.getAsDouble() * 100.0) / 100.0 : 0);
        stats.put("avgVolume", avgVolume.isPresent() ? Math.round(avgVolume.getAsDouble() * 100.0) / 100.0 : 0);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取林地列表（用于下拉选择）
     */
    @GetMapping("/forest-lands")
    public ResponseEntity<List<Map<String, Object>>> getForestLands() {
        Map<Long, ForestLand> allForestLands = dataGeneratorService.getForestLands();
        
        List<Map<String, Object>> forestLandOptions = allForestLands.values().stream()
            .map(forestLand -> {
                Map<String, Object> option = new HashMap<>();
                option.put("id", forestLand.getId());
                option.put("name", forestLand.getName());
                option.put("classification", forestLand.getClassification());
                return option;
            })
            .sorted((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(forestLandOptions);
    }
}