package com.forest.management.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资源监测数据实体类
 */
public class ResourceMonitor {
    private Long id;
    
    @NotNull(message = "关联林地ID不能为空")
    private Long forestLandId;
    
    private String forestLandName; // 林地名称（冗余字段，便于显示）
    
    @NotNull(message = "监测类型不能为空")
    private String monitorType; // 监测类型：生长量、蓄积量
    
    @NotNull(message = "当前值不能为空")
    @Positive(message = "当前值必须为正数")
    private Double currentValue; // 当前值
    
    private Double previousValue; // 上期值
    private Double changeRate; // 变化率(%)
    private Double changeAmount; // 变化量
    
    @NotNull(message = "监测日期不能为空")
    private LocalDate monitorDate; // 监测日期
    
    private String unit; // 单位
    private String remarks; // 备注
    private LocalDateTime createTime;
    
    // 构造函数
    public ResourceMonitor() {
        this.createTime = LocalDateTime.now();
    }
    
    public ResourceMonitor(Long forestLandId, String monitorType, Double currentValue, LocalDate monitorDate) {
        this();
        this.forestLandId = forestLandId;
        this.monitorType = monitorType;
        this.currentValue = currentValue;
        this.monitorDate = monitorDate;
    }
    
    // 计算变化率和变化量
    public void calculateChanges() {
        if (previousValue != null && previousValue > 0) {
            this.changeAmount = currentValue - previousValue;
            this.changeRate = (changeAmount / previousValue) * 100;
        }
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getForestLandId() { return forestLandId; }
    public void setForestLandId(Long forestLandId) { this.forestLandId = forestLandId; }
    
    public String getForestLandName() { return forestLandName; }
    public void setForestLandName(String forestLandName) { this.forestLandName = forestLandName; }
    
    public String getMonitorType() { return monitorType; }
    public void setMonitorType(String monitorType) { this.monitorType = monitorType; }
    
    public Double getCurrentValue() { return currentValue; }
    public void setCurrentValue(Double currentValue) { 
        this.currentValue = currentValue;
        calculateChanges();
    }
    
    public Double getPreviousValue() { return previousValue; }
    public void setPreviousValue(Double previousValue) { 
        this.previousValue = previousValue;
        calculateChanges();
    }
    
    public Double getChangeRate() { return changeRate; }
    public void setChangeRate(Double changeRate) { this.changeRate = changeRate; }
    
    public Double getChangeAmount() { return changeAmount; }
    public void setChangeAmount(Double changeAmount) { this.changeAmount = changeAmount; }
    
    public LocalDate getMonitorDate() { return monitorDate; }
    public void setMonitorDate(LocalDate monitorDate) { this.monitorDate = monitorDate; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}