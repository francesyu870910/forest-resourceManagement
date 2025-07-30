package com.forest.management.controller;

import com.forest.management.model.LoginRequest;
import com.forest.management.model.LoginResponse;
import com.forest.management.model.ResetPasswordRequest;
import com.forest.management.model.User;
import com.forest.management.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class AuthController extends BaseController {
    
    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            return success("登录成功", loginResponse);
        } catch (Exception e) {
            return error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 密码重置
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetRequest) {
        try {
            authService.resetPassword(resetRequest.getUsername(), resetRequest.getNewPassword());
            return success("密码重置成功", null);
        } catch (Exception e) {
            return error("密码重置失败: " + e.getMessage());
        }
    }

    /**
     * 验证Token
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return error("无效的Authorization头");
            }
            
            boolean isValid = authService.isTokenValid(token);
            if (isValid) {
                String username = authService.extractUsername(token);
                String role = authService.extractRole(token);
                
                Map<String, Object> data = new HashMap<>();
                data.put("valid", true);
                data.put("username", username);
                data.put("role", role);
                
                return success("Token有效", data);
            } else {
                return error("Token无效或已过期");
            }
        } catch (Exception e) {
            return error("Token验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return error("无效的Authorization头");
            }
            
            if (!authService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Token无效或已过期"));
            }
            
            User user = authService.getCurrentUser(token);
            
            // 创建用户信息响应（不包含密码）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole());
            userInfo.put("enabled", user.isEnabled());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("lastLoginAt", user.getLastLoginAt());
            
            return success("获取用户信息成功", userInfo);
        } catch (Exception e) {
            return error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出（客户端处理，服务端记录日志）
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            if (token != null && authService.isTokenValid(token)) {
                String username = authService.extractUsername(token);
                // 这里可以添加登出日志记录
                System.out.println("用户 " + username + " 已登出");
            }
            return success("登出成功", null);
        } catch (Exception e) {
            return success("登出成功", null); // 即使出错也返回成功，因为登出主要由客户端处理
        }
    }

    /**
     * 从Authorization头中提取Token
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}