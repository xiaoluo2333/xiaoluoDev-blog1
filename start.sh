#!/bin/bash
# ====================================
#  喵～ XiaoluoDev Blog 启动脚本 (Linux / Mac)
#  使用说明：chmod +x start.sh && ./start.sh
#  依赖：需要先安装 JDK 25 并配置 JAVA_HOME
# ====================================

echo ""
echo "===================================="
echo "  喵～ XiaoluoDev Blog 启动脚本"
echo "===================================="
echo ""

# ==================== 检查 Java 环境 ====================
# 喵～ 用 command -v 看看 java 在不在 PATH 里
# 如果找不到 java 命令，说明 JDK 没装或者没配环境变量
if ! command -v java &> /dev/null; then
    echo "[错误] 找不到 Java 环境喵！请先安装 JDK 25"
    echo "[错误] 安装后请确保 java 命令可以在终端中直接使用"
    echo "[提示] Ubuntu/Debian: sudo apt install openjdk-25-jdk"
    echo "[提示] macOS: brew install openjdk@25"
    echo "[提示] 也可以试试设置 JAVA_HOME 环境变量并添加到 PATH 中喵"
    exit 1
fi
echo "[✓] Java 环境就绪"

# ==================== 启动后端服务 ====================
# 喵～ 切换到 backend 目录并启动 Spring Boot 应用
# --spring.config.additional-location 指定外部配置文件，覆盖内置配置
# 这样 config/blog.yml 里的数据库密码等配置就能生效啦！
echo "[*] 正在启动博客后端喵～ 请稍候..."

# 喵～ 获取脚本所在目录的绝对路径，防止在其它目录执行时路径出错
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR/backend" || {
    echo "[错误] 找不到 backend/ 目录喵！请确认你在项目根目录下执行此脚本"
    exit 1
}

# 喵～ 检查 JAR 文件是否存在，避免启动时才发现没打包
JAR_FILE="target/blog-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "[错误] 找不到 $JAR_FILE 喵！请先在 backend/ 目录下执行 mvn package 打包"
    echo "[提示] 打包命令：cd backend && mvn clean package -DskipTests"
    exit 1
fi

# 喵～ 使用外部 YAML 配置启动 Spring Boot JAR
java -jar "$JAR_FILE" --spring.config.additional-location=file:"$SCRIPT_DIR/config/blog.yml"

# 喵～ 如果 Java 进程非正常退出，打印提示信息
if [ $? -ne 0 ]; then
    echo "[错误] 博客后端启动失败喵～ 请检查上面的错误日志看看哪里出了问题 (>_<)"
    exit 1
fi