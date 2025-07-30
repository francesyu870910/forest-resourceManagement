@echo off
echo Testing Authentication Endpoints
echo.

echo Starting Spring Boot application...
start /B mvn spring-boot:run -q

echo Waiting for application to start...
timeout /t 10 /nobreak > nul

echo.
echo Testing login endpoint...
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"

echo.
echo.
echo Testing health endpoint...
curl -X GET http://localhost:8080/api/health

echo.
echo.
echo Authentication endpoints test completed!
echo Press any key to stop the application...
pause > nul

echo Stopping application...
taskkill /F /IM java.exe > nul 2>&1