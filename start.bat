@echo off
chcp 65001 >nul
title XiaoluoDev Blog - 喵～

:: ====================================
::  喵～ XiaoluoDev Blog 启动脚本 (Windows)
::  使用说明：双击运行即可启动博客后端
::  依赖：需要先安装 JDK 25 并配置 JAVA_HOME
:: ====================================
echo.
echo ====================================
echo   喵～ XiaoluoDev Blog 启动脚本
echo ====================================
echo.

:: ==================== 检查 Java 环境 ====================
:: 喵～ 用 where 命令看看 java 在不在 PATH 里
:: 如果找不到 java 命令，说明 JDK 没装或者没配环境变量
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 找不到 Java 环境喵！请先安装 JDK 25
    echo [错误] 安装后请确保 java 命令可以在命令行中直接使用
    echo [提示] 也可以试试设置 JAVA_HOME 环境变量并添加到 PATH 中喵
    pause
    exit /b 1
)
echo [✓] Java 环境就绪

:: ==================== 启动后端服务 ====================
:: 喵～ 切换到 backend 目录并启动 Spring Boot 应用
:: --spring.config.additional-location 指定外部配置文件，覆盖内置配置
:: 这样 config/blog.yml 里的数据库密码等配置就能生效啦！
echo [*] 正在启动博客后端喵～ 请稍候...
cd /d "%~dp0backend"

:: 喵～ 使用外部 YAML 配置启动 Spring Boot JAR
:: blog-1.0.0.jar 是用 mvn package 打包好的
:: 如果没打包，请先在 backend/ 目录下执行 mvn package 喵
java -jar target/blog-1.0.0.jar --spring.config.additional-location=file:../config/blog.yml

:: 喵～ 如果 Java 进程退出了，暂停一下让用户看到错误信息
pause