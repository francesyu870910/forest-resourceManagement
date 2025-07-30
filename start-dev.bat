@echo off
chcp 65001 >nul
echo 启动森林资源管理系统开发环境...
echo.

:: 检查环境
echo 检查运行环境...

:: 检查Java
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Java环境，请安装JDK 17+
    pause
    exit /b 1
)

:: 检查Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Maven，请安装Maven 3.6+
    pause
    exit /b 1
)

:: 检查Node.js
node -v >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Node.js，请安装Node.js 16+
    pause
    exit /b 1
)

echo 环境检查通过
echo.

:: 启动后端
echo 启动后端服务 (Spring Boot)...
start "Forest-Backend" cmd /k "cd backend && mvn spring-boot:run"

:: 等待后端启动
echo 等待后端服务启动...
timeout /t 10 /nobreak >nul

:: 启动前端
echo 启动前端开发服务器 (Vite)...
start "Forest-Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo 系统启动中...
echo.
echo 请等待服务启动完成，然后访问:
echo - 前端开发服务器: http://localhost:5173
echo - 后端API服务: http://localhost:8080
echo - API健康检查: http://localhost:8080/api/health
echo.
echo 演示账户:
echo - 管理员: admin / admin123
echo - 普通用户: user / user123
echo.
echo 按任意键关闭此窗口...
pause >nul