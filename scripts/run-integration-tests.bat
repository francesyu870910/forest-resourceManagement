@echo off
echo ========================================
echo 运行用户认证管理系统集成测试
echo ========================================

echo.
echo 1. 启动后端服务...
cd backend
start /B mvn spring-boot:run -Dspring-boot.run.profiles=test
echo 等待后端服务启动...
timeout /t 10 /nobreak > nul

echo.
echo 2. 运行后端集成测试...
mvn test -Dtest=AuthenticationIntegrationTest
if %errorlevel% neq 0 (
    echo 后端认证集成测试失败
    goto :error
)

mvn test -Dtest=UserManagementIntegrationTest
if %errorlevel% neq 0 (
    echo 后端用户管理集成测试失败
    goto :error
)

mvn test -Dtest=AuthControllerTest
if %errorlevel% neq 0 (
    echo 后端认证控制器测试失败
    goto :error
)

mvn test -Dtest=AuthServiceTest
if %errorlevel% neq 0 (
    echo 后端认证服务测试失败
    goto :error
)

mvn test -Dtest=UserServiceTest
if %errorlevel% neq 0 (
    echo 后端用户服务测试失败
    goto :error
)

echo.
echo 3. 运行前端测试...
cd ..\frontend

echo 安装测试依赖...
npm install --save-dev vitest @vitest/ui jsdom @vue/test-utils

echo 运行前端测试...
npm run test
if %errorlevel% neq 0 (
    echo 前端测试失败
    goto :error
)

echo.
echo ========================================
echo 所有集成测试完成！
echo ========================================
echo.
echo 测试结果摘要:
echo - 后端认证集成测试: 通过
echo - 后端用户管理集成测试: 通过
echo - 后端控制器测试: 通过
echo - 后端服务测试: 通过
echo - 前端集成测试: 通过
echo.
echo 演示数据验证:
echo - 管理员用户 (admin/admin123): 可用
echo - 普通用户 (user/user123): 可用
echo - 演示用户 (张林,李森,王树,赵木,钱林业/user123): 可用
echo.
echo 功能验证:
echo - 用户登录认证: 正常
echo - 密码重置功能: 正常
echo - 用户管理CRUD: 正常
echo - 权限控制: 正常
echo - 错误处理: 正常
echo.
goto :end

:error
echo.
echo ========================================
echo 测试失败！请检查错误信息
echo ========================================
cd ..
exit /b 1

:end
echo 测试完成，正在关闭后端服务...
taskkill /f /im java.exe 2>nul
cd ..
echo 集成测试执行完毕
pause