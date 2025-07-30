package com.forest.management.service;

import com.forest.management.model.LoginRequest;
import com.forest.management.model.LoginResponse;
import com.forest.management.model.User;
import com.forest.management.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 认证服务类
 */
@Service
public class AuthService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录验证
     */
    public LoginResponse login(LoginRequest loginRequest) {
        // 查找用户
        Optional<User> userOpt = userService.findByUsername(loginRequest.getUsername());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        User user = userOpt.get();
        
        // 检查用户是否被禁用
        if (!user.isEnabled()) {
            throw new RuntimeException("用户账户已被禁用");
        }
        
        // 验证密码
        if (!userService.validatePassword(user, loginRequest.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        
        // 更新最后登录时间
        userService.updateLastLoginTime(user.getUsername());
        
        // 返回登录响应
        return new LoginResponse(
            token,
            user.getUsername(),
            user.getRole(),
            jwtUtil.getExpirationTime()
        );
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token, String username) {
        try {
            return jwtUtil.validateToken(token, username);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从Token中提取用户名
     */
    public String extractUsername(String token) {
        try {
            return jwtUtil.extractUsername(token);
        } catch (Exception e) {
            throw new RuntimeException("无效的Token");
        }
    }

    /**
     * 从Token中提取用户角色
     */
    public String extractRole(String token) {
        try {
            return jwtUtil.extractRole(token);
        } catch (Exception e) {
            throw new RuntimeException("无效的Token");
        }
    }

    /**
     * 检查Token是否有效
     */
    public boolean isTokenValid(String token) {
        try {
            return jwtUtil.isValidTokenFormat(token) && !jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 重置密码
     */
    public void resetPassword(String username, String newPassword) {
        // 检查用户是否存在
        if (!userService.existsByUsername(username)) {
            throw new RuntimeException("用户不存在");
        }
        
        // 重置密码
        userService.resetPassword(username, newPassword);
    }

    /**
     * 获取当前用户信息
     */
    public User getCurrentUser(String token) {
        String username = extractUsername(token);
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        return userOpt.get();
    }
}