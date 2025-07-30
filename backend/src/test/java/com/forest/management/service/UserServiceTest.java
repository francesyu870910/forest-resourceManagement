package com.forest.management.service;

import com.forest.management.model.User;
import com.forest.management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 */
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testGetAllUsers() {
        List<User> users = userService.getAllUsers();
        assertNotNull(users);
        assertTrue(users.size() >= 7); // 至少有7个演示用户
        
        // 验证包含预期的演示用户
        String[] expectedUsernames = {"admin", "user", "张林", "李森", "王树", "赵木", "钱林业"};
        for (String expectedUsername : expectedUsernames) {
            boolean found = users.stream()
                .anyMatch(user -> expectedUsername.equals(user.getUsername()));
            assertTrue(found, "应该包含用户: " + expectedUsername);
        }
        
        System.out.println("获取所有用户测试通过，用户数: " + users.size());
    }

    @Test
    public void testGetUserById() {
        // 获取一个存在的用户
        List<User> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
        
        User firstUser = users.get(0);
        Optional<User> foundUser = userService.findById(firstUser.getId());
        
        assertTrue(foundUser.isPresent());
        assertEquals(firstUser.getUsername(), foundUser.get().getUsername());
        assertEquals(firstUser.getRole(), foundUser.get().getRole());
        
        System.out.println("根据ID获取用户测试通过");
    }

    @Test
    public void testGetUserByIdNotFound() {
        Optional<User> user = userService.findById(99999L);
        assertFalse(user.isPresent());
        System.out.println("获取不存在用户测试通过");
    }

    @Test
    public void testCreateUser() {
        User createdUser = userService.createUser("测试创建用户", "testpassword123", "USER");
        
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("测试创建用户", createdUser.getUsername());
        assertEquals("USER", createdUser.getRole());
        assertTrue(createdUser.isEnabled());
        assertNotNull(createdUser.getCreatedAt());
        
        // 验证密码已加密
        assertNotEquals("testpassword123", createdUser.getPassword());
        assertTrue(createdUser.getPassword().startsWith("$2a$"));
        
        // 清理测试数据
        userService.deleteUser(createdUser.getId());
        System.out.println("创建用户测试通过");
    }

    @Test
    public void testCreateUserWithDuplicateUsername() {
        // 尝试创建与现有用户同名的用户
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser("admin", "password123", "USER");
        });
        
        assertEquals("用户名已存在", exception.getMessage());
        System.out.println("重复用户名创建测试通过");
    }

    @Test
    public void testUpdateUser() {
        // 先创建一个用户
        User createdUser = userService.createUser("测试更新用户", "password123", "USER");
        
        // 更新用户信息
        User updatedUser = userService.updateUser(createdUser.getId(), "测试更新用户", "ADMIN", true);
        
        assertNotNull(updatedUser);
        assertEquals("ADMIN", updatedUser.getRole());
        assertEquals(createdUser.getId(), updatedUser.getId());
        
        // 清理测试数据
        userService.deleteUser(createdUser.getId());
        System.out.println("更新用户测试通过");
    }

    @Test
    public void testUpdateNonExistentUser() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(99999L, "不存在用户", "USER", true);
        });
        
        assertEquals("用户不存在", exception.getMessage());
        System.out.println("更新不存在用户测试通过");
    }

    @Test
    public void testDeleteUser() {
        // 先创建一个用户
        User createdUser = userService.createUser("测试删除用户", "password123", "USER");
        Long userId = createdUser.getId();
        
        // 验证用户存在
        assertTrue(userService.findById(userId).isPresent());
        
        // 删除用户
        userService.deleteUser(userId);
        
        // 验证用户已被删除
        assertFalse(userService.findById(userId).isPresent());
        System.out.println("删除用户测试通过");
    }

    @Test
    public void testDeleteNonExistentUser() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(99999L);
        });
        
        assertEquals("用户不存在", exception.getMessage());
        System.out.println("删除不存在用户测试通过");
    }

    @Test
    public void testUserValidation() {
        // 测试用户名为空
        RuntimeException exception1 = assertThrows(RuntimeException.class, () -> {
            userService.createUser("", "password123", "USER");
        });
        assertTrue(exception1.getMessage().contains("用户名不能为空"));
        
        // 测试密码过短
        RuntimeException exception2 = assertThrows(RuntimeException.class, () -> {
            userService.createUser("测试用户", "123", "USER");
        });
        assertTrue(exception2.getMessage().contains("密码长度至少6个字符"));
        
        // 测试无效角色
        RuntimeException exception3 = assertThrows(RuntimeException.class, () -> {
            userService.createUser("测试用户", "password123", "INVALID");
        });
        assertTrue(exception3.getMessage().contains("无效的用户角色"));
        
        System.out.println("用户验证测试通过");
    }

    @Test
    public void testPasswordEncryption() {
        User createdUser = userService.createUser("密码加密测试", "plainpassword", "USER");
        
        // 验证密码已加密
        assertNotEquals("plainpassword", createdUser.getPassword());
        assertTrue(createdUser.getPassword().startsWith("$2a$"));
        
        // 验证可以通过加密密码验证原密码
        assertTrue(userService.validatePassword(createdUser, "plainpassword"));
        assertFalse(userService.validatePassword(createdUser, "wrongpassword"));
        
        // 清理测试数据
        userService.deleteUser(createdUser.getId());
        System.out.println("密码加密测试通过");
    }

    @Test
    public void testUserRoleValidation() {
        // 测试有效角色
        String[] validRoles = {"USER", "ADMIN"};
        for (String role : validRoles) {
            User createdUser = userService.createUser("角色测试" + role, "password123", role);
            assertEquals(role, createdUser.getRole());
            userService.deleteUser(createdUser.getId());
        }
        
        System.out.println("用户角色验证测试通过");
    }

    @Test
    public void testDemoUsersIntegrity() {
        // 验证所有演示用户的完整性
        String[][] expectedUsers = {
            {"admin", "ADMIN"},
            {"user", "USER"},
            {"张林", "USER"},
            {"李森", "USER"},
            {"王树", "ADMIN"},
            {"赵木", "USER"},
            {"钱林业", "USER"}
        };

        List<User> allUsers = userService.getAllUsers();
        
        for (String[] expectedUser : expectedUsers) {
            String username = expectedUser[0];
            String expectedRole = expectedUser[1];
            
            Optional<User> user = allUsers.stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst();
            
            assertTrue(user.isPresent(), "演示用户应该存在: " + username);
            assertEquals(expectedRole, user.get().getRole(), "用户角色应该正确: " + username);
            assertTrue(user.get().isEnabled(), "演示用户应该是启用状态: " + username);
            assertNotNull(user.get().getCreatedAt(), "创建时间不应为空: " + username);
        }
        
        System.out.println("演示用户完整性验证通过");
    }
}