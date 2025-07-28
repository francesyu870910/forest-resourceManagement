@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo 开始构建森林资源管理系统...

:: 检查环境
echo 检查构建环境...

:: 检查Java版本
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Java环境，请安装JDK 17+
    exit /b 1
)

:: 检查Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Maven，请安装Maven 3.6+
    exit /b 1
)

:: 检查Node.js
node -v >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Node.js，请安装Node.js 16+
    exit /b 1
)

:: 检查npm
npm -v >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到npm
    exit /b 1
)

echo 环境检查通过

:: 创建构建目录
set BUILD_DIR=build
if exist "%BUILD_DIR%" (
    echo 清理旧的构建目录...
    rmdir /s /q "%BUILD_DIR%"
)
mkdir "%BUILD_DIR%"

:: 构建后端
echo 构建后端项目...
cd backend

:: 清理并编译
call mvn clean compile
if errorlevel 1 (
    echo 错误: 后端编译失败
    exit /b 1
)

:: 运行测试
echo 运行后端测试...
call mvn test
if errorlevel 1 (
    echo 警告: 后端测试失败，但继续构建
)

:: 打包
echo 打包后端应用...
call mvn package -DskipTests
if errorlevel 1 (
    echo 错误: 后端打包失败
    exit /b 1
)

:: 复制后端构建产物
copy target\*.jar "..\%BUILD_DIR%\"
echo 后端构建完成

:: 构建前端
echo 构建前端项目...
cd ..\frontend

:: 安装依赖
echo 安装前端依赖...
call npm install
if errorlevel 1 (
    echo 错误: 前端依赖安装失败
    exit /b 1
)

:: 构建生产版本
echo 构建前端生产版本...
call npm run build
if errorlevel 1 (
    echo 错误: 前端构建失败
    exit /b 1
)

:: 复制前端构建产物
xcopy /e /i dist "..\%BUILD_DIR%\frontend"
echo 前端构建完成

:: 创建部署脚本
cd ..
(
echo @echo off
echo chcp 65001 ^>nul
echo echo 开始部署森林资源管理系统...
echo.
echo :: 检查Java环境
echo java -version ^>nul 2^>^&1
echo if errorlevel 1 ^(
echo     echo 错误: 未找到Java环境
echo     exit /b 1
echo ^)
echo.
echo :: 停止旧的服务
echo echo 停止旧的服务...
echo taskkill /f /im java.exe /fi "WINDOWTITLE eq forest-management*" ^>nul 2^>^&1
echo.
echo :: 启动后端服务
echo echo 启动后端服务...
echo start "forest-management" java -jar forest-management-*.jar
echo echo 后端服务已启动
echo.
echo :: 等待服务启动
echo timeout /t 5 /nobreak ^>nul
echo.
echo echo 前端文件已准备就绪，请配置Web服务器指向 frontend 目录
echo echo 部署完成！
echo echo 访问地址: http://localhost:8080
) > "%BUILD_DIR%\deploy.bat"

:: 创建停止脚本
(
echo @echo off
echo echo 停止森林资源管理系统...
echo taskkill /f /im java.exe /fi "WINDOWTITLE eq forest-management*"
echo echo 服务已停止
) > "%BUILD_DIR%\stop.bat"

:: 创建README
(
echo # 森林资源管理系统部署包
echo.
echo ## 部署说明
echo.
echo ### 环境要求
echo - Java 17+
echo - Web服务器 ^(IIS/Nginx/Apache，可选^)
echo.
echo ### 部署步骤
echo.
echo 1. **部署后端服务**
echo ```cmd
echo deploy.bat
echo ```
echo.
echo 2. **配置Web服务器** ^(可选^)
echo 将 `frontend` 目录配置为Web服务器的根目录
echo.
echo 3. **访问系统**
echo - 直接访问: http://localhost:8080
echo - 通过Web服务器: http://your-domain.com
echo.
echo ### 管理命令
echo.
echo - 停止服务: `stop.bat`
echo - 检查状态: 访问 http://localhost:8080/api/overview/system-status
echo.
echo ### 故障排除
echo.
echo 1. **端口被占用**
echo    - 检查8080端口是否被其他程序占用
echo    - 使用 `netstat -ano ^| findstr :8080` 查看端口占用
echo.
echo 2. **服务启动失败**
echo    - 检查Java版本是否正确
echo    - 查看控制台错误信息
echo.
echo 3. **前端无法访问后端**
echo    - 检查防火墙设置
echo    - 确认后端服务正常运行
) > "%BUILD_DIR%\README.md"

echo.
echo 构建完成！
echo 构建产物位于: %BUILD_DIR%\
echo 包含文件:
dir "%BUILD_DIR%"
echo.
echo 部署说明:
echo 1. 将 %BUILD_DIR% 目录复制到目标服务器
echo 2. 运行 deploy.bat 启动服务
echo 3. 配置Web服务器指向 frontend 目录 ^(可选^)
echo.
pause