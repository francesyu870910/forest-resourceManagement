package com.forest.management.service;

import com.forest.management.model.*;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 演示数据生成服务
 */
@Service
public class DataGeneratorService {
    
    // 内存存储
    private final Map<Long, TreeArchive> treeArchives = new ConcurrentHashMap<>();
    private final Map<Long, ForestLand> forestLands = new ConcurrentHashMap<>();
    private final Map<Long, ResourceMonitor> resourceMonitors = new ConcurrentHashMap<>();
    private final Map<Long, ForestRights> forestRights = new ConcurrentHashMap<>();
    private final Map<Long, CuttingPermit> cuttingPermits = new ConcurrentHashMap<>();
    
    // ID生成器
    private final AtomicLong treeIdGenerator = new AtomicLong(1);
    private final AtomicLong forestLandIdGenerator = new AtomicLong(1);
    private final AtomicLong monitorIdGenerator = new AtomicLong(1);
    private final AtomicLong rightsIdGenerator = new AtomicLong(1);
    private final AtomicLong permitIdGenerator = new AtomicLong(1);
    
    // 基础数据
    private final String[] treeSpecies = {
        "松树", "杉树", "柏树", "桦树", "榆树", "槐树", "梧桐", "银杏", 
        "樟树", "桂花树", "枫树", "柳树", "杨树", "橡树", "栗树"
    };
    
    private final String[] healthStatuses = {"健康", "良好", "一般", "较差", "病虫害"};
    private final String[] forestClassifications = {"用材林", "防护林", "经济林"};
    private final String[] monitorTypes = {"生长量", "蓄积量"};
    private final String[] locations = {
        "东山林区", "西山林区", "南山林区", "北山林区", "中央林区",
        "青龙山", "白虎岭", "朱雀峰", "玄武谷", "麒麟坡"
    };
    
    private final String[] ownerNames = {
        "张建国", "李明华", "王志强", "赵德胜", "钱国庆", "孙永刚", "周建军", "吴海涛",
        "郑文博", "王春雷", "冯国强", "陈志华", "褚建平", "卫东升", "刘国栋", "杨志军",
        "朱建华", "秦明亮", "尤志远", "许建设", "何国良", "吕文军", "施建民", "张永强",
        "孔德华", "曹志刚", "严建国", "华春林", "金国平", "魏志华", "陶建军", "姜文强"
    };
    
    private final String[] cuttingReasons = {
        "林木更新改造", "病虫害防治", "防火隔离带建设", "基础设施建设",
        "林分改良", "抚育间伐", "卫生伐", "更新伐"
    };
    
    @PostConstruct
    public void initData() {
        generateForestLands();
        generateTreeArchives();
        generateResourceMonitors();
        generateForestRights();
        generateCuttingPermits();
    }
    
    /**
     * 生成林地数据
     */
    private void generateForestLands() {
        Random random = new Random();
        
        for (int i = 0; i < 50; i++) {
            ForestLand forestLand = new ForestLand();
            forestLand.setId(forestLandIdGenerator.getAndIncrement());
            forestLand.setName(locations[random.nextInt(locations.length)] + "第" + (i + 1) + "片区");
            forestLand.setClassification(forestClassifications[random.nextInt(forestClassifications.length)]);
            forestLand.setArea(50.0 + random.nextDouble() * 500.0); // 50-550公顷
            forestLand.setLocation(locations[random.nextInt(locations.length)]);
            forestLand.setCoordinates(generateCoordinates(random));
            forestLand.setDescription("该林地位于" + forestLand.getLocation() + "，总面积" + 
                String.format("%.2f", forestLand.getArea()) + "公顷");
            
            forestLands.put(forestLand.getId(), forestLand);
        }
    }
    
    /**
     * 生成林木档案数据
     */
    private void generateTreeArchives() {
        Random random = new Random();
        List<Long> forestLandIds = new ArrayList<>(forestLands.keySet());
        
        for (int i = 0; i < 500; i++) {
            TreeArchive tree = new TreeArchive();
            tree.setId(treeIdGenerator.getAndIncrement());
            tree.setTreeSpecies(treeSpecies[random.nextInt(treeSpecies.length)]);
            tree.setDiameter(5.0 + random.nextDouble() * 45.0); // 5-50cm
            tree.setHeight(2.0 + random.nextDouble() * 28.0); // 2-30m
            tree.setHealthStatus(healthStatuses[random.nextInt(healthStatuses.length)]);
            
            // 关联到随机林地
            Long forestLandId = forestLandIds.get(random.nextInt(forestLandIds.size()));
            ForestLand forestLand = forestLands.get(forestLandId);
            tree.setLocation(forestLand.getName());
            
            treeArchives.put(tree.getId(), tree);
        }
    }
    
    /**
     * 生成资源监测数据
     */
    private void generateResourceMonitors() {
        Random random = new Random();
        List<Long> forestLandIds = new ArrayList<>(forestLands.keySet());
        
        // 为每个林地生成12个月的监测数据
        for (Long forestLandId : forestLandIds) {
            ForestLand forestLand = forestLands.get(forestLandId);
            
            for (String monitorType : monitorTypes) {
                double baseValue = "生长量".equals(monitorType) ? 
                    random.nextDouble() * 10 + 5 : // 生长量 5-15
                    random.nextDouble() * 1000 + 500; // 蓄积量 500-1500
                
                for (int month = 1; month <= 12; month++) {
                    ResourceMonitor monitor = new ResourceMonitor();
                    monitor.setId(monitorIdGenerator.getAndIncrement());
                    monitor.setForestLandId(forestLandId);
                    monitor.setForestLandName(forestLand.getName());
                    monitor.setMonitorType(monitorType);
                    
                    // 模拟季节性变化
                    double seasonalFactor = 1.0 + 0.2 * Math.sin((month - 1) * Math.PI / 6);
                    double currentValue = baseValue * seasonalFactor * (0.9 + random.nextDouble() * 0.2);
                    
                    monitor.setCurrentValue(currentValue);
                    monitor.setMonitorDate(LocalDate.of(2024, month, 15));
                    monitor.setUnit("生长量".equals(monitorType) ? "cm/年" : "立方米");
                    
                    // 设置上期值和计算变化
                    if (month > 1) {
                        double previousValue = baseValue * (1.0 + 0.2 * Math.sin((month - 2) * Math.PI / 6)) * 
                            (0.9 + random.nextDouble() * 0.2);
                        monitor.setPreviousValue(previousValue);
                    }
                    
                    resourceMonitors.put(monitor.getId(), monitor);
                }
            }
        }
    }
    
    /**
     * 生成林权证书数据
     */
    private void generateForestRights() {
        Random random = new Random();
        List<Long> forestLandIds = new ArrayList<>(forestLands.keySet());
        
        for (int i = 0; i < 100; i++) {
            ForestRights rights = new ForestRights();
            rights.setId(rightsIdGenerator.getAndIncrement());
            rights.setCertificateNo("林权证" + String.format("%06d", i + 1));
            rights.setOwnerName(ownerNames[random.nextInt(ownerNames.length)]);
            rights.setOwnerIdCard(generateIdCard(random));
            rights.setOwnerPhone(generatePhone(random));
            
            // 关联林地
            Long forestLandId = forestLandIds.get(random.nextInt(forestLandIds.size()));
            ForestLand forestLand = forestLands.get(forestLandId);
            rights.setForestLandId(forestLandId);
            rights.setForestLandName(forestLand.getName());
            
            // 设置日期
            LocalDate issueDate = LocalDate.now().minusYears(random.nextInt(10));
            rights.setIssueDate(issueDate);
            rights.setExpiryDate(issueDate.plusYears(20 + random.nextInt(30)));
            
            // 设置状态
            if (rights.isExpired()) {
                rights.setStatus("过期");
            } else if (rights.isExpiringSoon()) {
                rights.setStatus("即将到期");
            } else {
                rights.setStatus("有效");
            }
            
            rights.setIssueOrgan("市林业局");
            
            forestRights.put(rights.getId(), rights);
        }
    }
    
    /**
     * 生成采伐许可数据
     */
    private void generateCuttingPermits() {
        Random random = new Random();
        List<Long> forestLandIds = new ArrayList<>(forestLands.keySet());
        String[] statuses = {"待审批", "已批准", "已拒绝"};
        
        for (int i = 0; i < 80; i++) {
            CuttingPermit permit = new CuttingPermit();
            permit.setId(permitIdGenerator.getAndIncrement());
            permit.setPermitNo("采伐许可" + String.format("%06d", i + 1));
            permit.setApplicantName(ownerNames[random.nextInt(ownerNames.length)]);
            permit.setApplicantPhone(generatePhone(random));
            permit.setApplicantIdCard(generateIdCard(random));
            
            // 关联林地
            Long forestLandId = forestLandIds.get(random.nextInt(forestLandIds.size()));
            ForestLand forestLand = forestLands.get(forestLandId);
            permit.setForestLandId(forestLandId);
            permit.setForestLandName(forestLand.getName());
            
            permit.setCuttingArea(1.0 + random.nextDouble() * 10.0); // 1-11公顷
            permit.setCuttingVolume(50.0 + random.nextDouble() * 500.0); // 50-550立方米
            permit.setReason(cuttingReasons[random.nextInt(cuttingReasons.length)]);
            
            // 设置申请日期
            permit.setApplicationDate(LocalDate.now().minusDays(random.nextInt(365)));
            
            // 设置状态和审批信息
            String status = statuses[random.nextInt(statuses.length)];
            permit.setStatus(status);
            
            if (!"待审批".equals(status)) {
                permit.setApprover("审批员" + (random.nextInt(5) + 1));
                permit.setApprovalDate(permit.getApplicationDate().plusDays(random.nextInt(30)));
                
                if ("已批准".equals(status)) {
                    permit.setApprovalOpinion("符合采伐条件，予以批准");
                    permit.setValidUntil(permit.getApprovalDate().plusYears(1));
                } else {
                    permit.setApprovalOpinion("不符合采伐条件，不予批准");
                }
            }
            
            cuttingPermits.put(permit.getId(), permit);
        }
    }
    
    // 辅助方法
    private String generateCoordinates(Random random) {
        double lat = 30.0 + random.nextDouble() * 20.0; // 纬度 30-50
        double lng = 100.0 + random.nextDouble() * 40.0; // 经度 100-140
        return String.format("%.6f,%.6f", lat, lng);
    }
    
    private String generateIdCard(Random random) {
        return "4101" + String.format("%02d", random.nextInt(12) + 1) + 
               String.format("%02d", random.nextInt(28) + 1) + 
               "19" + String.format("%02d", random.nextInt(50) + 50) + 
               String.format("%04d", random.nextInt(10000));
    }
    
    private String generatePhone(Random random) {
        return "1" + (3 + random.nextInt(6)) + String.format("%09d", random.nextInt(1000000000));
    }
    
    // Getter方法供Controller使用
    public Map<Long, TreeArchive> getTreeArchives() { return treeArchives; }
    public Map<Long, ForestLand> getForestLands() { return forestLands; }
    public Map<Long, ResourceMonitor> getResourceMonitors() { return resourceMonitors; }
    public Map<Long, ForestRights> getForestRights() { return forestRights; }
    public Map<Long, CuttingPermit> getCuttingPermits() { return cuttingPermits; }
    
    // ID生成器
    public Long generateTreeId() { return treeIdGenerator.getAndIncrement(); }
    public Long generateForestLandId() { return forestLandIdGenerator.getAndIncrement(); }
    public Long generateMonitorId() { return monitorIdGenerator.getAndIncrement(); }
    public Long generateRightsId() { return rightsIdGenerator.getAndIncrement(); }
    public Long generatePermitId() { return permitIdGenerator.getAndIncrement(); }
}