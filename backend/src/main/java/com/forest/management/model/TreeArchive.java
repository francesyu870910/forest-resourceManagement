package com.forest.management.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * 林木资源档案实体类
 */
public class TreeArchive {
    private Long id;
    
    @NotBlank(message = "树种不能为空")
    private String treeSpecies;
    
    @NotNull(message = "胸径不能为空")
    @Positive(message = "胸径必须为正数")
    private Double diameter;
    
    @NotNull(message = "树高不能为空")
    @Positive(message = "树高必须为正数")
    private Double height;
    
    @NotBlank(message = "健康状态不能为空")
    private String healthStatus;
    
    private String location;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 构造函数
    public TreeArchive() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    public TreeArchive(String treeSpecies, Double diameter, Double height, String healthStatus, String location) {
        this();
        this.treeSpecies = treeSpecies;
        this.diameter = diameter;
        this.height = height;
        this.healthStatus = healthStatus;
        this.location = location;
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTreeSpecies() { return treeSpecies; }
    public void setTreeSpecies(String treeSpecies) { this.treeSpecies = treeSpecies; }
    
    public Double getDiameter() { return diameter; }
    public void setDiameter(Double diameter) { this.diameter = diameter; }
    
    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
    
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}