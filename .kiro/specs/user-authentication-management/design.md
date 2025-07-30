# 设计文档

## 概述

为森林资源管理系统设计用户认证和管理功能，包括登录页面、密码重置和用户管理模块。设计采用前后端分离架构，前端使用Vue 3 + Element Plus，后端使用Spring Boot。

## 架构

### 整体架构
- **前端**: Vue 3 + Vue Router + Element Plus + Axios
- **后端**: Spring Boot + RESTful API
- **认证方式**: JWT Token
- **数据存储**: 内存存储（演示版本）

### 认证流程
1. 用户在登录页面输入凭据
2. 前端发送登录请求到后端API
3. 后端验证凭据并生成JWT Token
4. 前端存储Token并设置路由守卫
5. 后续请求携带Token进行身份验证

## 组件和接口

### 前端组件

#### 1. 登录组件 (Login.vue)
- **位置**: `frontend/src/components/auth/Login.vue`
- **功能**: 用户登录表单，包含用户名、密码输入和忘记密码链接
- **状态管理**: 使用Vue 3 Composition API管理登录状态

#### 2. 密码重置组件 (ResetPassword.vue)
- **位置**: `frontend/src/components/auth/ResetPassword.vue`
- **功能**: 密码重置表单，包含用户名验证和新密码设置

#### 3. 用户管理组件 (UserManagement.vue)
- **位置**: `frontend/src/components/management/UserManagement.vue`
- **功能**: 用户列表展示、添加、编辑、删除用户

#### 4. 用户表单组件 (UserForm.vue)
- **位置**: `frontend/src/components/management/UserForm.vue`
- **功能**: 用户信息表单，支持新增和编辑模式

### 后端接口

#### 1. 认证控制器 (AuthController)
- **位置**: `backend/src/main/java/com/forest/management/controller/AuthController.java`
- **接口**:
  - `POST /api/auth/login` - 用户登录
  - `POST /api/auth/reset-password` - 密码重置
  - `POST /api/auth/logout` - 用户登出

#### 2. 用户管理控制器 (UserController)
- **位置**: `backend/src/main/java/com/forest/management/controller/UserController.java`
- **接口**:
  - `GET /api/users` - 获取用户列表
  - `POST /api/users` - 创建用户
  - `PUT /api/users/{id}` - 更新用户
  - `DELETE /api/users/{id}` - 删除用户

### 服务层

#### 1. 认证服务 (AuthService)
- **位置**: `backend/src/main/java/com/forest/management/service/AuthService.java`
- **功能**: 处理用户认证逻辑、JWT Token生成和验证

#### 2. 用户服务 (UserService)
- **位置**: `backend/src/main/java/com/forest/management/service/UserService.java`
- **功能**: 用户CRUD操作、密码加密

## 数据模型

### 用户模型 (User)
```java
public class User {
    private Long id;
    private String username;
    private String password; // 加密存储
    private String role; // ADMIN, USER
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
```

### 登录请求模型 (LoginRequest)
```java
public class LoginRequest {
    private String username;
    private String password;
}
```

### 登录响应模型 (LoginResponse)
```java
public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private long expiresIn;
}
```

### 密码重置请求模型 (ResetPasswordRequest)
```java
public class ResetPasswordRequest {
    private String username;
    private String newPassword;
}
```

## 路由设计

### 前端路由更新
在现有路由基础上添加：
- `/login` - 登录页面
- `/reset-password` - 密码重置页面
- `/users` - 用户管理页面（管理员权限）

### 路由守卫
- 未登录用户访问受保护路由时重定向到登录页面
- 已登录用户访问登录页面时重定向到主页
- 管理员权限验证

## 错误处理

### 前端错误处理
- 网络请求失败提示
- 表单验证错误显示
- 权限不足提示

### 后端错误处理
- 统一异常处理器
- 标准化错误响应格式
- 日志记录

## 测试策略

### 前端测试
- 组件单元测试
- 路由导航测试
- API调用测试

### 后端测试
- 控制器集成测试
- 服务层单元测试
- 认证流程测试

## 安全考虑

### 密码安全
- 使用BCrypt加密存储密码
- 密码复杂度验证（演示版本简化）

### Token安全
- JWT Token设置合理过期时间
- Token在localStorage中存储
- 请求拦截器自动添加Authorization头

### 前端安全
- 路由守卫防止未授权访问
- 敏感操作二次确认

## 演示数据

### 预设用户
- 管理员: `admin/admin123`
- 普通用户: `user/user123`

### 功能演示流程
1. 展示登录页面和认证流程
2. 演示密码重置功能
3. 展示用户管理界面
4. 演示权限控制效果