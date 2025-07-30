package com.forest.management.service;

import com.forest.management.model.User;
import com.forest.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户管理服务
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 创建新用户
     */
    public User createUser(String username, String password, String role) {
        // 验证用户名
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new RuntimeException("用户名长度必须在3-50个字符之间");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 验证密码
        if (password == null || password.length() < 6) {
            throw new RuntimeException("密码长度至少6个字符");
        }
        
        // 验证角色
        if (role == null || (!role.equals("ADMIN") && !role.equals("USER"))) {
            throw new RuntimeException("无效的用户角色");
        }
        
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 更新用户信息
     */
    public User updateUser(Long id, String username, String role, Boolean enabled) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 验证用户名
        if (username != null) {
            if (username.trim().isEmpty()) {
                throw new RuntimeException("用户名不能为空");
            }
            if (username.length() < 3 || username.length() > 50) {
                throw new RuntimeException("用户名长度必须在3-50个字符之间");
            }
            // 检查用户名是否被其他用户使用
            if (!user.getUsername().equals(username.trim()) && userRepository.existsByUsername(username.trim())) {
                throw new RuntimeException("用户名已被其他用户使用");
            }
            user.setUsername(username.trim());
        }
        
        // 验证角色
        if (role != null) {
            if (!role.equals("ADMIN") && !role.equals("USER")) {
                throw new RuntimeException("无效的用户角色");
            }
            user.setRole(role);
        }
        
        if (enabled != null) {
            user.setEnabled(enabled);
        }
        
        return userRepository.save(user);
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        if (!userRepository.findById(id).isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    /**
     * 验证用户密码
     */
    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * 重置用户密码
     */
    public void resetPassword(String username, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证新密码
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("密码长度至少6个字符");
        }
        
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 更新用户最后登录时间
     */
    public void updateLastLoginTime(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 获取用户总数
     */
    public long getUserCount() {
        return userRepository.count();
    }
}