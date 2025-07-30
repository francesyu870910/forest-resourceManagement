package com.forest.management.model;

/**
 * 用户角色枚举
 */
public enum UserRole {
    ADMIN("ADMIN", "管理员"),
    USER("USER", "普通用户");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取角色
     */
    public static UserRole fromCode(String code) {
        for (UserRole role : UserRole.values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("无效的用户角色: " + code);
    }

    /**
     * 验证角色是否有效
     */
    public static boolean isValidRole(String code) {
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取所有可用角色
     */
    public static UserRole[] getAllRoles() {
        return UserRole.values();
    }
}