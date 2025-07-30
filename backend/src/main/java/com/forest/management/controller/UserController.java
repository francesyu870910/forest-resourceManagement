package com.forest.management.controller;

import com.forest.management.model.User;
import com.forest.management.service.AuthService;
import com.forest.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class UserController extends BaseController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthService authService;

    /**
     * 获取所有用户列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        try {
            // 验证Token和权限
            if (!validateAdminAccess(authHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("需要管理员权限"));
            }
            
            List<User> users = userService.getAllUsers();
            return success("获取用户列表成功", users);
        } catch (Exception e) {
            return error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证Token
            if (!validateTokenAccess(authHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Token无效或已过期"));
            }
            
            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isEmpty()) {
                return error("用户不存在");
            }
            
            return success("获取用户信息成功", userOpt.get());
        } catch (Exception e) {
            return error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 创建新用户
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证Token和权限
            if (!validateAdminAccess(authHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("需要管理员权限"));
            }
            
            User user = userService.createUser(request.getUsername(), request.getPassword(), request.getRole());
            return success("创建用户成功", user);
        } catch (Exception e) {
            return error("创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证Token和权限
            if (!validateAdminAccess(authHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("需要管理员权限"));
            }
            
            User user = userService.updateUser(id, request.getUsername(), request.getRole(), request.getEnabled());
            return success("更新用户成功", user);
        } catch (Exception e) {
            return error("更新用户失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 验证Token和权限
            if (!validateAdminAccess(authHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("需要管理员权限"));
            }
            
            userService.deleteUser(id);
            return success("删除用户成功", null);
        } catch (Exception e) {
            return error("删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@RequestHeader("Authorization") String authHeader) {
        try {
            // 验证Token和权限
            if (!validateAdminAccess(authHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("需要管理员权限"));
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userService.getUserCount());
            stats.put("timestamp", System.currentTimeMillis());
            
            return success("获取用户统计成功", stats);
        } catch (Exception e) {
            return error("获取用户统计失败: " + e.getMessage());
        }
    }

    /**
     * 验证Token访问权限
     */
    private boolean validateTokenAccess(String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            return token != null && authService.isTokenValid(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证管理员访问权限
     */
    private boolean validateAdminAccess(String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            if (token == null || !authService.isTokenValid(token)) {
                return false;
            }
            
            String role = authService.extractRole(token);
            return "ADMIN".equals(role);
        } catch (Exception e) {
            return false;
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

    /**
     * 创建用户请求模型
     */
    public static class CreateUserRequest {
        private String username;
        private String password;
        private String role;

        // Getter和Setter方法
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    /**
     * 更新用户请求模型
     */
    public static class UpdateUserRequest {
        private String username;
        private String role;
        private Boolean enabled;

        // Getter和Setter方法
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }
}