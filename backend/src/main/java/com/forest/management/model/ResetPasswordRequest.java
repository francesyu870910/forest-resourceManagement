package com.forest.management.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 密码重置请求模型
 */
public class ResetPasswordRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "密码长度至少6个字符")
    private String newPassword;

    // 构造函数
    public ResetPasswordRequest() {}

    public ResetPasswordRequest(String username, String newPassword) {
        this.username = username;
        this.newPassword = newPassword;
    }

    // Getter和Setter方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public String toString() {
        return "ResetPasswordRequest{" +
                "username='" + username + '\'' +
                '}';
    }
}