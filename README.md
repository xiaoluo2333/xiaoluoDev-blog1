# XiaoluoDev Blog 🐱

一个基于 Spring Boot 3.x + Vue 3 + TypeScript 的极简风格个人博客平台，
集成插件热加载系统和隐性下载站。

---

## ⚠️ 重要声明

**本项目由 AI（Trae-CN IDE）辅助生成**，可能会包含未发现的 Bug 或设计缺陷。

本项目和代码仅供参考和学习使用。如果你要部署到生产环境，请务必：
- 仔细审查代码安全性
- 修改默认密码（admin/admin）
- 配置 HTTPS
- 做好数据备份

> 使用愉快喵～ (｀・ω・´)

---

## 功能特性

| 模块 | 说明 |
|------|------|
| 📝 博客系统 | Markdown + WYSIWYG 混合编辑器（Vditor），文章管理 |
| 🔐 双重认证 | 用户 Cookie（1天/7天可配置）+ Admin Cookie（1小时硬编码） |
| 🗃️ 文件管理 | 文件夹树 + 上传/下载 + 防路径穿越安全校验 |
| 🔌 插件系统 | 类似 Minecraft Bukkit 的 JAR 热加载，plugin.yml 声明 |
| 🎨 极简设计 | TailwindCSS + 自定义样式，响应式布局 |
| 🔒 安全密码 | Argon2id 哈希存储密码 |

## 技术栈

| 层级 | 技术 |
|------|------|
| 🖥️ 后端 | Spring Boot 3.4.5 + Spring Security 6.x + Spring Data JPA |
| 🗄️ 数据库 | MySQL 8.0 |
| 🎨 前端 | Vue 3 + TypeScript + Webpack + TailwindCSS 3 |
| ✍️ 编辑器 | Vditor（Markdown + WYSIWYG） |
| 🔐 安全 | Argon2id 密码哈希 + JWT Token + HttpOnly Cookie |
| 🔌 插件 | 自定义 ClassLoader 热加载 + plugin.yml 声明机制 |

## 快速开始

### 环境要求

- JDK 25+
- Maven 3.9+
- MySQL 8.0
- Node.js 24+（仅前端开发）

### 1. 创建数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS xiaoluoDev_blog_db DEFAULT CHARACTER SET utf8mb4;"
```

### 2. 编译前端

```bash
cd frontend
npm install
npm run build
```

### 3. 编译后端

```bash
cd backend
mvn clean package -DskipTests
```

### 4. 启动

```bash
# Windows
start.bat

# Linux/Mac
chmod +x start.sh && ./start.sh

# 或者手动启动
java -jar backend/target/blog-1.0.0.jar --spring.config.additional-location=file:config/blog.yml
```

### 5. 访问

- 博客主页：http://localhost:8080
- 管理员后台：http://localhost:8080/admin
- 默认管理员：admin / admin（**首次部署请修改密码！**）

## 项目结构

```
├── backend/                         # Spring Boot 后端
│   ├── src/main/java/xl/xldevblog/main/
│   │   ├── controller/             # REST API + 页面路由
│   │   ├── service/                # 业务逻辑
│   │   ├── repository/             # 数据访问
│   │   ├── entity/                 # 数据库实体
│   │   ├── dto/                    # 数据传输对象
│   │   ├── security/               # JWT + Spring Security
│   │   ├── plugin/                 # 插件系统核心
│   │   └── config/                 # 配置 + 数据初始化
│   └── src/main/resources/
│       ├── templates/              # HTML 模板
│       └── application.properties  # JAR 内置配置
├── frontend/                       # Vue 3 + TypeScript 前端
│   ├── src/
│   │   ├── pages/                  # 页面逻辑
│   │   ├── api/                    # API 封装
│   │   ├── utils/                  # 工具函数
│   │   └── types/                  # TypeScript 类型
│   ├── css/                        # 分离的样式文件
│   └── webpack.config.js           # 构建配置
├── hello-world-plugin/             # 示例插件（Maven 独立项目）
├── config/
│   └── blog.yml                    # 外部配置（YAML 格式）
├── plugins/                        # 插件存放目录
├── uploads/                        # 上传文件目录
├── downloads/                      # 下载文件目录
├── start.bat                       # Windows 启动脚本
├── start.sh                        # Linux/Mac 启动脚本
└── README.md
```

## API 概览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录（设置 X-User-Token） |
| POST | `/api/auth/admin/login` | 管理员登录（设置 X-Admin-Token） |
| GET | `/api/posts` | 获取已发布文章列表 |
| GET | `/api/posts/{slug}` | 获取文章详情 |
| POST | `/api/posts` | 创建文章（需管理员） |
| GET | `/api/files` | 获取文件树 |
| GET | `/api/files/download/{id}` | 下载文件 |
| POST | `/api/files/upload` | 上传文件（需管理员） |
| GET | `/api/plugins` | 获取已加载插件列表 |
| POST | `/api/plugins/reload` | 重新加载插件（需管理员） |

## Cookie 策略

| Cookie | 用户 | 有效期 | 可配置 |
|--------|------|--------|--------|
| `X-User-Token` | 普通用户 | 默认1天 / 记住我7天 | config/blog.yml |
| `X-Admin-Token` | 管理员 | **1小时（硬编码）** | **不可配置！改代码才能改** |

## 插件开发

插件使用 `plugin.yml` 声明信息，实现 `Plugin` 接口即可。

详见 [hello-world-plugin](hello-world-plugin/) 示例项目。

## License

MIT License

---

项目由 [Trae-CN](https://www.trae.com.cn) IDE 辅助生成 🐱