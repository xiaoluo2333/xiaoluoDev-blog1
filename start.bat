@echo off
chcp 65001 >nul
title XiaoluoDev Blog

echo.
echo ====================================
echo   XiaoluoDev Blog
echo ====================================
echo.

where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java not found! Please install JDK 25.
    echo [ERROR] Make sure java is in your PATH.
    pause
    exit /b 1
)
echo [OK] Java ready

echo [*] Checking JAR file...
if not exist "backend\target\blog-1.0.0.jar" (
    echo [WARN] JAR not found, building project first...
    cd /d "%~dp0backend"
    call mvn package -DskipTests
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Build failed!
        pause
        exit /b 1
    )
    cd /d "%~dp0"
)
echo [OK] JAR ready

cd /d "%~dp0backend"
echo [*] Starting blog backend...
java -jar target/blog-1.0.0.jar --spring.config.additional-location=file:../config/blog.yml

pause