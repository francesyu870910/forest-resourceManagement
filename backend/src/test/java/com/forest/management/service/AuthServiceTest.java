package com.forest.management.service;

import com.forest.management.model.LoginRequest;
import com.forest.management.model.LoginResponse;
import com.forest.management.model.User;
import com.forest.management.repository.UserRepository;
import com.forest.management.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证服务测试
 */
@SpringBootTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    public void testSuccessfulLogin() {
        // 测试管理员登录
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");
        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("admin", response.getUsername());
        assertEquals("ADMIN", response.getRole());
        assertNotNull(response.getToken());
        assertTrue(response.getExpiresIn() > 0);
    }

    @Test
    public void testUserLogin() {
        // 测试普通用户登录
        LoginRequest loginRequest = new LoginRequest("user", "user123");
        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("user", response.getUsername());
        assertEquals("USER", response.getRole());
        assertNotNull(response.getToken());
    }

    @Test
    public void testInvalidCredentials() {
        // 测试错误密码
        LoginRequest loginRequest = new LoginRequest("admin", "wrongpassword");
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    public void testNonExistentUser() {
        // 测试不存在的用户
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password");
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    public void testTokenValidation() {
        // 登录获取Token
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");
        LoginResponse response = authService.login(loginRequest);
        String token = response.getToken();

        // 验证Token
        assertTrue(authService.validateToken(token, "admin"));
        assertFalse(authService.validateToken(token, "wronguser"));
        assertTrue(authService.isTokenValid(token));
    }

    @Test
    public void testExtractUserInfoFromToken() {
        // 登录获取Token
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");
        LoginResponse response = authService.login(loginRequest);
        String token = response.getToken();

        // 从Token中提取信息
        assertEquals("admin", authService.extractUsername(token));
        assertEquals("ADMIN", authService.extractRole(token));
    }

    @Test
    public void testPasswordReset() {
        // 重置密码
        String newPassword = "newpassword123";
        authService.resetPassword("user", newPassword);

        // 使用新密码登录
        LoginRequest loginRequest = new LoginRequest("user", newPassword);
        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("user", response.getUsername());
    }

    @Test
    public void testResetPasswordForNonExistentUser() {
        // 为不存在的用户重置密码
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.resetPassword("nonexistent", "newpassword");
        });
        
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    public void testGetCurrentUser() {
        // 登录获取Token
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");
        LoginResponse response = authService.login(loginRequest);
        String token = response.getToken();

        // 获取当前用户信息
        User currentUser = authService.getCurrentUser(token);
        assertNotNull(currentUser);
        assertEquals("admin", currentUser.getUsername());
        assertEquals("ADMIN", currentUser.getRole());
    }
}