package com.forest.management.service;

import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

/**
 * 数据验证服务
 */
@Service
public class ValidationService {
    
    // 手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    // 身份证号正则表达式
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[\\dXx]$");
    
    // 证书编号正则表达式
    private static final Pattern CERTIFICATE_PATTERN = Pattern.compile("^[A-Za-z0-9\\u4e00-\\u9fa5]{6,20}$");
    
    /**
     * 验证手机号格式
     */
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * 验证身份证号格式
     */
    public boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return false;
        }
        return ID_CARD_PATTERN.matcher(idCard.trim()).matches();
    }
    
    /**
     * 验证证书编号格式
     */
    public boolean isValidCertificateNo(String certificateNo) {
        if (certificateNo == null || certificateNo.trim().isEmpty()) {
            return false;
        }
        return CERTIFICATE_PATTERN.matcher(certificateNo.trim()).matches();
    }
    
    /**
     * 验证树种名称
     */
    public boolean isValidTreeSpecies(String treeSpecies) {
        if (treeSpecies == null || treeSpecies.trim().isEmpty()) {
            return false;
        }
        String trimmed = treeSpecies.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 20;
    }
    
    /**
     * 验证健康状态
     */
    public boolean isValidHealthStatus(String healthStatus) {
        if (healthStatus == null || healthStatus.trim().isEmpty()) {
            return false;
        }
        String[] validStatuses = {"健康", "良好", "一般", "较差", "病虫害"};
        for (String status : validStatuses) {
            if (status.equals(healthStatus.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 验证林地分类
     */
    public boolean isValidForestClassification(String classification) {
        if (classification == null || classification.trim().isEmpty()) {
            return false;
        }
        String[] validClassifications = {"用材林", "防护林", "经济林"};
        for (String cls : validClassifications) {
            if (cls.equals(classification.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 验证监测类型
     */
    public boolean isValidMonitorType(String monitorType) {
        if (monitorType == null || monitorType.trim().isEmpty()) {
            return false;
        }
        String[] validTypes = {"生长量", "蓄积量"};
        for (String type : validTypes) {
            if (type.equals(monitorType.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 验证证书状态
     */
    public boolean isValidRightsStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String[] validStatuses = {"有效", "过期", "注销", "即将到期"};
        for (String validStatus : validStatuses) {
            if (validStatus.equals(status.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 验证许可状态
     */
    public boolean isValidPermitStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String[] validStatuses = {"待审批", "已批准", "已拒绝", "已过期"};
        for (String validStatus : validStatuses) {
            if (validStatus.equals(status.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 验证数值范围
     */
    public boolean isValidRange(Double value, double min, double max) {
        if (value == null) {
            return false;
        }
        return value >= min && value <= max;
    }
    
    /**
     * 验证胸径范围 (5-100cm)
     */
    public boolean isValidDiameter(Double diameter) {
        return isValidRange(diameter, 5.0, 100.0);
    }
    
    /**
     * 验证树高范围 (1-50m)
     */
    public boolean isValidHeight(Double height) {
        return isValidRange(height, 1.0, 50.0);
    }
    
    /**
     * 验证面积范围 (0.1-10000公顷)
     */
    public boolean isValidArea(Double area) {
        return isValidRange(area, 0.1, 10000.0);
    }
    
    /**
     * 验证采伐量范围 (1-10000立方米)
     */
    public boolean isValidCuttingVolume(Double volume) {
        return isValidRange(volume, 1.0, 10000.0);
    }
    
    /**
     * 验证字符串长度
     */
    public boolean isValidLength(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        int length = str.trim().length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * 验证姓名格式
     */
    public boolean isValidName(String name) {
        return isValidLength(name, 2, 20) && 
               name.trim().matches("^[\\u4e00-\\u9fa5a-zA-Z]+$");
    }
    
    /**
     * 验证地址格式
     */
    public boolean isValidLocation(String location) {
        return isValidLength(location, 2, 100);
    }
}