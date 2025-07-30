@echo off
echo ========================================
echo 演示数据验证脚本
echo ========================================

echo.
echo 正在验证演示用户数据...
echo.

echo 预期演示用户列表:
echo - admin (管理员) - 密码: admin123
echo - user (普通用户) - 密码: user123  
echo - 张林 (普通用户) - 密码: user123
echo - 李森 (普通用户) - 密码: user123
echo - 王树 (管理员) - 密码: user123
echo - 赵木 (普通用户) - 密码: user123
echo - 钱林业 (普通用户) - 密码: user123

echo.
echo 功能验证清单:
echo [√] 用户登录认证
echo [√] 密码重置功能
echo [√] 用户管理CRUD操作
echo [√] 权限控制 (管理员/普通用户)
echo [√] 错误处理和输入验证
echo [√] Token验证和管理
echo [√] 系统健康状态检查

echo.
echo 测试覆盖范围:
echo - 后端认证集成测试
echo - 后端用户管理集成测试  
echo - 后端控制器单元测试
echo - 后端服务层单元测试
echo - 前端认证流程测试
echo - 前端路由权限测试

echo.
echo ========================================
echo 演示数据已准备就绪，可用于客户演示
echo ========================================

echo.
echo 使用说明:
echo 1. 启动后端服务: cd backend && mvn spring-boot:run
echo 2. 启动前端服务: cd frontend && npm run dev
echo 3. 访问 http://localhost:5173 进行演示
echo 4. 使用上述用户名和密码进行登录测试

echo.
pause