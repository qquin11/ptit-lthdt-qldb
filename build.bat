@echo off
chcp 65001 >nul
cd /d "%~dp0"

where mvn >nul 2>&1
if errorlevel 1 (
    echo Khong tim thay Maven. Cai dat tu https://maven.apache.org
    pause
    exit /b 1
)

call mvn clean package -DskipTests
if errorlevel 1 (
    echo Build that bai.
    pause
    exit /b 1
)

echo.
echo Build xong. Chay start.bat de mo app.
pause
