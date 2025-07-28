package com.forest.management.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采伐许可实体类
 */
public class CuttingPermit {
    private Long id;
    
    @NotBlank(message = "许可证编号不能为空")
    private String permitNo; // 许可证编号
    
    @NotBlank(message = "申请人姓名不能为空")
    private String applicantName; // 申请人姓名
    
    private String applicantPhone; // 申请人电话
    private String applicantIdCard; // 申请人身份证
    
    @NotNull(message = "采伐地点不能为空")
    private Long forestLandId; // 采伐地点ID
    
    private String forestLandName; // 林地名称（冗余字段）
    
    @NotNull(message = "采伐面积不能为空")
    @Positive(message = "采伐面积必须为正数")
    private Double cuttingArea; // 采伐面积(公顷)
    
    @NotNull(message = "采伐量不能为空")
    @Positive(message = "采伐量必须为正数")
    private Double cuttingVolume; // 采伐量(立方米)
    
    @NotBlank(message = "申请理由不能为空")
    private String reason; // 申请理由
    
    @NotBlank(message = "状态不能为空")
    private String status; // 状态：待审批、已批准、已拒绝、已过期
    
    private String approvalOpinion; // 审批意见
    private String approver; // 审批人
    
    @NotNull(message = "申请日期不能为空")
    private LocalDate applicationDate; // 申请日期
    
    private LocalDate approvalDate; // 审批日期
    private LocalDate validUntil; // 有效期至
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 构造函数
    public CuttingPermit() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = "待审批";
        this.applicationDate = LocalDate.now();
    }
    
    public CuttingPermit(String permitNo, String applicantName, Long forestLandId, 
                        Double cuttingArea, Double cuttingVolume, String reason) {
        this();
        this.permitNo = permitNo;
        this.applicantName = applicantName;
        this.forestLandId = forestLandId;
        this.cuttingArea = cuttingArea;
        this.cuttingVolume = cuttingVolume;
        this.reason = reason;
    }
    
    // 审批通过
    public void approve(String approver, String opinion) {
        this.status = "已批准";
        this.approver = approver;
        this.approvalOpinion = opinion;
        this.approvalDate = LocalDate.now();
        this.validUntil = LocalDate.now().plusYears(1); // 有效期1年
        this.updateTime = LocalDateTime.now();
    }
    
    // 审批拒绝
    public void reject(String approver, String opinion) {
        this.status = "已拒绝";
        this.approver = approver;
        this.approvalOpinion = opinion;
        this.approvalDate = LocalDate.now();
        this.updateTime = LocalDateTime.now();
    }
    
    // 检查是否即将到期
    public boolean isExpiringSoon() {
        if (validUntil == null || !"已批准".equals(status)) return false;
        return validUntil.isBefore(LocalDate.now().plusDays(30));
    }
    
    // 检查是否已过期
    public boolean isExpired() {
        if (validUntil == null || !"已批准".equals(status)) return false;
        return validUntil.isBefore(LocalDate.now());
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPermitNo() { return permitNo; }
    public void setPermitNo(String permitNo) { this.permitNo = permitNo; }
    
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    
    public String getApplicantPhone() { return applicantPhone; }
    public void setApplicantPhone(String applicantPhone) { this.applicantPhone = applicantPhone; }
    
    public String getApplicantIdCard() { return applicantIdCard; }
    public void setApplicantIdCard(String applicantIdCard) { this.applicantIdCard = applicantIdCard; }
    
    public Long getForestLandId() { return forestLandId; }
    public void setForestLandId(Long forestLandId) { this.forestLandId = forestLandId; }
    
    public String getForestLandName() { return forestLandName; }
    public void setForestLandName(String forestLandName) { this.forestLandName = forestLandName; }
    
    public Double getCuttingArea() { return cuttingArea; }
    public void setCuttingArea(Double cuttingArea) { this.cuttingArea = cuttingArea; }
    
    public Double getCuttingVolume() { return cuttingVolume; }
    public void setCuttingVolume(Double cuttingVolume) { this.cuttingVolume = cuttingVolume; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getApprovalOpinion() { return approvalOpinion; }
    public void setApprovalOpinion(String approvalOpinion) { this.approvalOpinion = approvalOpinion; }
    
    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }
    
    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }
    
    public LocalDate getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }
    
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}