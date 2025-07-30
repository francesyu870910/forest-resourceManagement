@echo off
chcp 65001 >nul
echo 停止森林资源管理系统开发环境...

:: 停止Java进程 (后端)
echo 停止后端服务...
taskkill /F /IM java.exe >nul 2>&1

:: 停止Node.js进程 (前端)
echo 停止前端服务...
taskkill /F /IM node.exe >nul 2>&1

:: 关闭相关的cmd窗口
taskkill /F /FI "WINDOWTITLE eq Forest-Backend*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Forest-Frontend*" >nul 2>&1

echo 所有服务已停止
pause