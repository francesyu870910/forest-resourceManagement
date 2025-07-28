package com.forest.management.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 林权证书实体类
 */
public class ForestRights {
    private Long id;
    
    @NotBlank(message = "证书编号不能为空")
    private String certificateNo; // 证书编号
    
    @NotBlank(message = "权利人姓名不能为空")
    private String ownerName; // 权利人姓名
    
    private String ownerIdCard; // 权利人身份证
    private String ownerPhone; // 权利人电话
    
    @NotNull(message = "关联林地ID不能为空")
    private Long forestLandId; // 关联林地ID
    
    private String forestLandName; // 林地名称（冗余字段）
    
    @NotNull(message = "发证日期不能为空")
    private LocalDate issueDate; // 发证日期
    
    @NotNull(message = "到期日期不能为空")
    private LocalDate expiryDate; // 到期日期
    
    @NotBlank(message = "证书状态不能为空")
    private String status; // 状态：有效、过期、注销
    
    private String issueOrgan; // 发证机关
    private String remarks; // 备注
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 构造函数
    public ForestRights() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = "有效";
    }
    
    public ForestRights(String certificateNo, String ownerName, Long forestLandId, 
                       LocalDate issueDate, LocalDate expiryDate) {
        this();
        this.certificateNo = certificateNo;
        this.ownerName = ownerName;
        this.forestLandId = forestLandId;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
    }
    
    // 检查是否即将到期（30天内）
    public boolean isExpiringSoon() {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now().plusDays(30));
    }
    
    // 检查是否已过期
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now());
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCertificateNo() { return certificateNo; }
    public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public String getOwnerIdCard() { return ownerIdCard; }
    public void setOwnerIdCard(String ownerIdCard) { this.ownerIdCard = ownerIdCard; }
    
    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }
    
    public Long getForestLandId() { return forestLandId; }
    public void setForestLandId(Long forestLandId) { this.forestLandId = forestLandId; }
    
    public String getForestLandName() { return forestLandName; }
    public void setForestLandName(String forestLandName) { this.forestLandName = forestLandName; }
    
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getIssueOrgan() { return issueOrgan; }
    public void setIssueOrgan(String issueOrgan) { this.issueOrgan = issueOrgan; }
    
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}