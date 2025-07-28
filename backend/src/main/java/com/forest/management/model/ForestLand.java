package com.forest.management.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * 林地信息实体类
 */
public class ForestLand {
    private Long id;
    
    @NotBlank(message = "林地名称不能为空")
    private String name;
    
    @NotBlank(message = "林地分类不能为空")
    private String classification; // 用材林、防护林、经济林
    
    @NotNull(message = "面积不能为空")
    @Positive(message = "面积必须为正数")
    private Double area; // 面积(公顷)
    
    private String location; // 位置描述
    private String coordinates; // 坐标信息
    private String description; // 描述信息
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 构造函数
    public ForestLand() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    public ForestLand(String name, String classification, Double area, String location) {
        this();
        this.name = name;
        this.classification = classification;
        this.area = area;
        this.location = location;
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
    
    public Double getArea() { return area; }
    public void setArea(Double area) { this.area = area; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getCoordinates() { return coordinates; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}