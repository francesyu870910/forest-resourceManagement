package com.forest.management.repository;

import com.forest.management.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户数据存储库（内存实现，用于演示）
 */
@Repository
public class UserRepository {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Map<String, Long> usernameToId = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserRepository() {
        // 初始化演示数据
        initDemoData();
    }

    /**
     * 初始化演示用户数据
     */
    private void initDemoData() {
        // 创建管理员用户 - 使用BCrypt加密密码
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User admin = new User("admin", encoder.encode("admin123"), "ADMIN"); // 密码: admin123
        admin.setId(idGenerator.getAndIncrement());
        users.put(admin.getId(), admin);
        usernameToId.put(admin.getUsername(), admin.getId());

        // 创建普通用户
        User user = new User("user", encoder.encode("user123"), "USER"); // 密码: user123
        user.setId(idGenerator.getAndIncrement());
        users.put(user.getId(), user);
        usernameToId.put(user.getUsername(), user.getId());

        // 创建更多演示用户
        String[] demoUsernames = {"张林", "李森", "王树", "赵木", "钱林业"};
        String[] demoRoles = {"USER", "USER", "ADMIN", "USER", "USER"};
        
        for (int i = 0; i < demoUsernames.length; i++) {
            User demoUser = new User(demoUsernames[i], encoder.encode("user123"), demoRoles[i]); // 密码: user123
            demoUser.setId(idGenerator.getAndIncrement());
            users.put(demoUser.getId(), demoUser);
            usernameToId.put(demoUser.getUsername(), demoUser.getId());
        }
    }

    /**
     * 保存用户
     */
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        users.put(user.getId(), user);
        usernameToId.put(user.getUsername(), user.getId());
        return user;
    }

    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        Long id = usernameToId.get(username);
        return id != null ? Optional.ofNullable(users.get(id)) : Optional.empty();
    }

    /**
     * 获取所有用户
     */
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    /**
     * 删除用户
     */
    public void deleteById(Long id) {
        User user = users.remove(id);
        if (user != null) {
            usernameToId.remove(user.getUsername());
        }
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return usernameToId.containsKey(username);
    }

    /**
     * 获取用户总数
     */
    public long count() {
        return users.size();
    }
}