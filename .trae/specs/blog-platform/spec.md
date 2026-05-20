# 个人博客 + 下载站平台 - 产品需求文档 (PRD)

## Overview
- **Summary**: 基于 Spring Boot 3.x + Vue3 + TypeScript 的极简风格个人博客平台，集成插件热加载系统和隐性下载站，支持多用户注册与独立管理员 Cookie 策略
- **Purpose**: 提供一个可扩展、安全、美观的个人博客解决方案，支持插件热加载和文件管理，普通用户和 admin 采用两套独立的 Cookie 策略
- **Target Users**: 普通访客（注册/浏览/下载）、站长（管理后台）

## Goals
- ✅ 极简美观的博客主页，专注内容呈现
- ✅ 安全的 Admin 后台管理（Argon2id 密码加密）
- ✅ 两套独立的 JWT Cookie 系统：用户侧（1天~7天可配置）+ Admin 侧（1小时硬编码）
- ✅ 换 IP 不丢失登录状态
- ✅ 类似 Minecraft 的 JAR 插件热加载系统
- ✅ 隐性下载站（文章附件 → 文件浏览器）
- ✅ 支持 Windows/Linux/Mac 路径配置
- ✅ Markdown + WYSIWYG 混合编辑器
- ✅ 多用户注册，管理员唯一

## Non-Goals (Out of Scope)
- ❌ 评论系统（可通过插件扩展）
- ❌ 复杂权限管理（仅普通用户/管理员二层）
- ❌ 国际化多语言（仅中文）
- ❌ OAuth 第三方登录

## Background & Context
- 环境：Java 25, Node.js 24, MySQL 8.0（已安装运行）
- 数据库：root / 35433123f，库名 xiaoluoDev_blog_db
- 仓库：GitHub https://github.com/xiaoluo2333/xiaoluoDev-blog1
- 要求：混合模式架构、TypeScript编译、轻量HTML多页面、外部配置用 yml（非 properties）、JAR 内置配置保持 properties

## Functional Requirements

### FR-1: 博客主页
- 展示文章列表（标题、摘要、日期、标签、作者）
- 支持分页
- 响应式设计

### FR-2: 文章详情页
- 渲染 Markdown 内容
- 显示附件列表（可点击进入下载站）
- 代码高亮支持

### FR-3: Admin 后台
- 管理员登录页（仅 admin 可登录）
- 文章管理（CRUD）
- 文件管理（创建文件夹、上传文件）
- 插件管理（查看已加载插件）
- Cookie 过期策略设置（用户侧 1天/7天可配置）

### FR-4: 用户系统
- 普通用户注册（用户名+密码+邮箱）
- 普通用户登录（1天默认，勾选"记住我"=7天）
- 个人主页（查看自己的信息）
- 管理员唯一（通过 is_admin 字段标识，系统初始化时创建）

### FR-5: 插件系统
- JAR 文件热加载（plugins/*.jar）
- 插件可注册路由和页面
- 插件可提供下载文件
- HelloWorld 示例插件

### FR-6: 隐性下载站
- 文章附件链接触发文件浏览器
- 类似 openlist 的文件夹树展示
- 支持文件下载
- ⚠️ 文件路径必须经过安全校验，防止路径穿越攻击

### FR-7: 认证系统
- Argon2id 密码哈希存储
- 两套独立的 JWT Cookie 策略：
  - 用户 Cookie：「X-User-Token」1天默认 / 勾选记住我=7天（配置可调）
  - Admin Cookie：「X-Admin-Token」1小时硬编码，无法通过配置延长
- 换 IP 不丢失登录状态

## Non-Functional Requirements

### NFR-1: 安全性
- 密码使用 Argon2id 哈希（内存成本 65536KB，迭代 3 次，并行度 4）
- 用户 Cookie 有效期：默认 1 天，可勾选"记住我"→7 天（配置可调）
- Admin Cookie 有效期：1 小时，**代码级硬编码**，改代码才能改
- Cookie 设置 HttpOnly、Secure、SameSite=Lax（允许跨页面携带）
- 防止 CSRF 攻击
- 注册时邮箱格式校验
- ⚠️ **路径穿越防护**：所有文件操作（上传/下载/列出/删除）必须校验路径合法性：
  - 使用 `File.getCanonicalPath()` 解析为绝对路径后校验
  - 拒绝任何包含 `..`、`~` 等特殊字符的路径
  - 确保解析后的路径在允许的基目录（uploads/ 或 downloads/）下
  - 对 Windows 和 Linux 路径格式分别处理

### NFR-2: 性能
- 首页加载时间 < 2 秒
- 数据库连接池配置合理
- 静态资源缓存优化

### NFR-3: 可用性
- 极简设计，高可读性
- 响应式布局（移动端适配）
- 清晰的视觉层次

### NFR-4: 可扩展性
- 插件系统支持热加载
- 模块化代码结构
- 配置文件集中管理

### NFR-5: 代码注释规范
- 所有类、方法、关键逻辑必须包含详细注释
- 注释风格：轻猫娘语风，自然可爱但不刻意角色扮演
- 注释要解释"为什么这样做"而非仅仅"做了什么"

## Constraints

### Technical
- Java 25 + Spring Boot 3.x
- Vue3 + TypeScript
- MySQL 8.0（账号 root，密码 35433123f）
- Maven 构建
- 外部配置文件使用 `.yml` 格式（绝对不用 .properties）
- JAR 包内部配置文件保持 `.properties` 格式

### Business
- 管理员唯一（系统初始化时创建）
- 本地部署优先

### Dependencies
- Spring Security 6.x
- jjwt (JWT)
- argon2-jvm (密码哈希)
- Vditor (编辑器)
- Vue Router

## Assumptions
- MySQL 服务已运行，库名 xiaoluoDev_blog_db（应用启动时自动创建）
- 用户熟悉基础命令行操作
- 插件开发者了解 Java 开发
- 管理员首次启动时通过配置或初始化脚本创建

## Acceptance Criteria

### AC-1: 用户注册
- **Given**: 访客访问注册页面
- **When**: 输入用户名、密码、邮箱并提交
- **Then**: 注册成功，用户表新增记录，密码 Argon2id 哈希存储
- **Verification**: `programmatic`

### AC-2: 用户登录（1天）
- **Given**: 已注册用户访问登录页
- **When**: 输入正确凭据，不勾选"记住我"
- **Then**: 登录成功，设置 X-User-Token Cookie，有效期 1 天
- **Verification**: `programmatic`

### AC-3: 用户登录（7天）
- **Given**: 已注册用户访问登录页
- **When**: 输入正确凭据并勾选"记住我"
- **Then**: 登录成功，设置 X-User-Token Cookie，有效期 7 天
- **Verification**: `programmatic`

### AC-4: Admin 登录
- **Given**: 管理员访问 /admin/login
- **When**: 输入正确的管理员用户名密码
- **Then**: 登录成功，设置 X-Admin-Token Cookie，有效期 1 小时
- **Verification**: `programmatic`

### AC-5: Admin Cookie 硬编码
- **Given**: 管理员已登录
- **When**: 修改配置文件中 Cookie 过期时间
- **Then**: Admin Cookie 仍然 1 小时过期（硬编码不受配置影响）
- **Verification**: `programmatic`

### AC-6: Cookie 跨 IP
- **Given**: 用户已登录（Cookie 已设置）
- **When**: 更换网络/IP 后访问网站
- **Then**: 用户保持登录状态
- **Verification**: `human-judgment`

### AC-7: 密码安全性
- **Given**: 用户或管理员创建/修改密码
- **When**: 系统存储密码
- **Then**: 密码使用 Argon2id 哈希存储，无明文存储
- **Verification**: `programmatic`

### AC-8: 插件热加载
- **Given**: 系统运行中
- **When**: 将 HelloWorld.jar 放入 plugins/ 目录
- **Then**: 系统自动加载插件，可访问 /plugin/hello 路径
- **Verification**: `programmatic`

### AC-9: 隐性下载站
- **Given**: 文章包含附件
- **When**: 点击附件链接
- **Then**: 打开类似 openlist 的文件浏览器，显示文件树
- **Verification**: `human-judgment`

### AC-10: 文章编辑
- **Given**: 管理员登录后台
- **When**: 创建/编辑文章
- **Then**: 使用 Vditor 编辑器，支持 Markdown 和 WYSIWYG
- **Verification**: `human-judgment`

### AC-11: 文件路径支持
- **Given**: 后台配置文件路径
- **When**: 输入 Windows 路径（D:\files）或 Linux 路径（/opt/files）
- **Then**: 系统正确识别并访问文件
- **Verification**: `programmatic`

### AC-12: 管理员唯一
- **Given**: 系统已有一个管理员
- **When**: 尝试通过 API 再次创建管理员
- **Then**: 请求被拒绝，提示管理员已存在
- **Verification**: `programmatic`

## Open Questions
- [x] 架构方案：已确定方案一（Spring Boot + Vue3 混合模式）
- [x] 插件系统：已确定 JAR 热加载
- [x] 下载站：已确定隐性设计（文章附件 → 文件浏览器）
- [x] 编辑器：已确定混合模式（Vditor）
- [x] Cookie 策略：用户 1天/7天 + Admin 1小时硬编码
- [x] 多用户：支持注册，管理员唯一
- [x] 配置格式：外部 yml，JAR 内部 properties

---

## 技术架构

### 整体架构图
```
┌─────────────────────────────────────────────────────────────┐
│                     前端层 (Vue3 + TS)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │   主页组件   │  │   文章组件   │  │    Admin组件    │  │
│  │ (Vue3+TS)   │  │ (Vue3+TS)   │  │   (Vue3+TS)     │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
│         │                  │                   │            │
│  ┌──────▼──────────────────▼───────────────────▼──────────┐ │
│  │               API 网关层 (统一 /api/*)                  │ │
│  │  用户 API: X-User-Token  |  Admin API: X-Admin-Token   │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot 3.x 后端层                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  BlogController│  │ AdminController│  │ PluginManager   │  │
│  │   (REST API)  │  │   (REST API)  │  │ (热加载服务)    │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
│         │                  │                   │            │
│  ┌──────▼───────┐  ┌──────▼───────┐  ┌────────▼─────────┐  │
│  │   BlogService │  │ FileService  │  │ AuthService     │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
│         │                  │                   │            │
│         ▼                  ▼                   ▼            │
│  ┌───────────────────────────────────────────────┐          │
│  │              Spring Data JPA                  │          │
│  │  ┌───────┐ ┌───────┐ ┌───────┐ ┌───────────┐ │          │
│  │  │Post   │ │File   │ │Plugin │ │  User    │ │          │
│  │  │       │ │       │ │       │ │(含Admin) │ │          │
│  │  └───────┘ └───────┘ └───────┘ └───────────┘ │          │
│  └───────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
              ┌───────────────────┐
              │    MySQL 8.0      │
              └───────────────────┘
```

### 目录结构
```
awa_nya/                             # 项目根目录
├── backend/                         # Spring Boot 后端
│   ├── src/main/java/xl/xldevblog/main/
│   │   ├── controller/             # REST API 控制层
│   │   │   ├── AuthController.java      # 认证相关 API
│   │   │   ├── PostController.java      # 文章 API
│   │   │   ├── FileController.java      # 文件 API
│   │   │   ├── AdminController.java     # 管理后台 API
│   │   │   └── PluginController.java    # 插件管理 API
│   │   ├── service/                # 业务逻辑层
│   │   │   ├── AuthService.java        # 认证服务
│   │   │   ├── PostService.java        # 文章服务
│   │   │   ├── FileService.java        # 文件服务
│   │   │   └── PluginService.java      # 插件服务
│   │   ├── repository/             # 数据访问层
│   │   │   ├── UserRepository.java
│   │   │   ├── PostRepository.java
│   │   │   ├── FileAttachmentRepository.java
│   │   │   └── PluginInfoRepository.java
│   │   ├── entity/                 # 数据库实体
│   │   │   ├── User.java
│   │   │   ├── Post.java
│   │   │   ├── FileAttachment.java
│   │   │   └── PluginInfo.java
│   │   ├── dto/                    # 数据传输对象
│   │   ├── config/                 # 配置类
│   │   ├── security/               # 安全相关
│   │   │   ├── UserJwtAuthFilter.java    # 用户 JWT 过滤器
│   │   │   ├── AdminJwtAuthFilter.java   # Admin JWT 过滤器
│   │   │   └── SecurityConfig.java        # 安全配置
│   │   ├── plugin/                 # 插件系统
│   │   │   ├── Plugin.java            # 插件接口
│   │   │   ├── PluginManager.java     # 插件管理器
│   │   │   └── PluginClassLoader.java # 热加载类加载器
│   │   └── BlogApplication.java    # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml         # 内部配置文件（编译时保持 .properties 格式）
│   │   └── templates/              # Thymeleaf 模板
│   ├── plugins/                    # 插件目录（运行时创建）
│   └── pom.xml                     # Maven 依赖

├── frontend/                       # Vue3 前端
│   ├── src/
│   │   ├── components/             # 公共组件
│   │   ├── views/                  # 页面视图
│   │   ├── api/                    # API 调用
│   │   └── main.ts                 # 入口文件
│   ├── package.json
│   ├── tsconfig.json
│   └── webpack.config.js           # 编译配置

├── config/                         # 外部配置目录（非 properties 格式）
│   └── blog.yml                    # 博客主配置文件

├── downloads/                      # 下载文件存储（运行时创建）
├── uploads/                        # 上传文件存储（运行时创建）
├── .gitignore
└── README.md
```

### 核心数据表设计

#### user（用户表 — 含普通用户和管理员）
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| password_hash | VARCHAR(255) | NOT NULL | Argon2id 哈希密码 |
| email | VARCHAR(100) | UNIQUE | 邮箱 |
| display_name | VARCHAR(100) | | 显示名称 |
| is_admin | BOOLEAN | NOT NULL, DEFAULT FALSE | 是否为管理员（系统唯一） |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

#### post（文章表）
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| author_id | BIGINT | FOREIGN KEY(user.id) | 作者 ID |
| title | VARCHAR(255) | NOT NULL | 标题 |
| slug | VARCHAR(255) | NOT NULL, UNIQUE | URL 别名 |
| content | LONGTEXT | NOT NULL | Markdown 内容 |
| summary | VARCHAR(500) | | 摘要 |
| tags | VARCHAR(500) | | 标签（逗号分隔） |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'DRAFT' | 状态(DRAFT/PUBLISHED) |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

#### file_attachment（文件附件表）
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| post_id | BIGINT | FOREIGN KEY(post.id) | 关联文章ID（可为 null，独立文件） |
| file_path | VARCHAR(1000) | NOT NULL | 文件路径（支持 Windows/Linux） |
| file_name | VARCHAR(255) | NOT NULL | 文件名 |
| file_size | BIGINT | NOT NULL | 文件大小(字节) |
| content_type | VARCHAR(100) | | MIME 类型 |
| is_folder | BOOLEAN | NOT NULL, DEFAULT FALSE | 是否为文件夹 |
| parent_id | BIGINT | FOREIGN KEY(id) | 父文件夹 ID |
| created_at | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### plugin_info（插件信息表）
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| plugin_name | VARCHAR(100) | NOT NULL | 插件名称 |
| plugin_id | VARCHAR(100) | NOT NULL, UNIQUE | 插件唯一标识 |
| version | VARCHAR(20) | NOT NULL | 版本号 |
| author | VARCHAR(100) | | 作者 |
| description | VARCHAR(500) | | 描述 |
| enabled | BOOLEAN | NOT NULL, DEFAULT TRUE | 是否启用 |
| loaded_at | DATETIME | | 加载时间 |

### API 接口设计

#### 用户认证接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | /api/auth/register | 用户注册 | 否 |
| POST | /api/auth/login | 用户登录（返回 X-User-Token） | 否 |
| POST | /api/auth/logout | 用户登出 | 是（用户） |
| GET | /api/auth/me | 获取当前用户信息 | 是（用户） |
| POST | /api/auth/admin/login | 管理员登录（返回 X-Admin-Token） | 否 |
| POST | /api/auth/admin/logout | 管理员登出 | 是（admin） |
| GET | /api/auth/admin/me | 获取管理员信息 | 是（admin） |

#### 文章接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | /api/posts | 获取文章列表（分页） | 否 |
| GET | /api/posts/{slug} | 获取单篇文章 | 否 |
| POST | /api/posts | 创建文章 | 是（admin） |
| PUT | /api/posts/{id} | 更新文章 | 是（admin） |
| DELETE | /api/posts/{id} | 删除文章 | 是（admin） |

#### 文件接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | /api/files | 获取文件树（下载站） | 否 |
| GET | /api/files/{parentId} | 获取指定文件夹内容 | 否 |
| GET | /api/files/download/{id} | 下载文件 | 否 |
| POST | /api/files/upload | 上传文件 | 是（admin） |
| POST | /api/files/folder | 创建文件夹 | 是（admin） |
| DELETE | /api/files/{id} | 删除文件/文件夹 | 是（admin） |

⚠️ 所有文件 API 内部必须校验路径，防止路径穿越（详见 NFR-1 安全策略）

#### 插件接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | /api/plugins | 获取已加载插件列表 | 是（admin） |
| POST | /api/plugins/reload | 重新加载所有插件 | 是（admin） |

#### 后台配置接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | /api/admin/config | 获取系统配置 | 是（admin） |
| PUT | /api/admin/config | 更新系统配置（含 Cookie 策略） | 是（admin） |

### Cookie 设计（双重 Cookie 策略）

系统维护两套完全独立的 JWT Cookie，分别针对普通用户和管理员，互不干扰。

#### 用户 Cookie：「X-User-Token」
```
Cookie 名称: X-User-Token
Cookie 值:   JWT Token（Base64URL 编码）
Cookie 属性: HttpOnly, Secure（生产环境）, SameSite=Lax
默认有效期:  1 天（86400 秒）
记住我有效期: 7 天（604800 秒，可在外部 config/blog.yml 中调整）
JWT Payload:
{
  "sub": "user_123",         // 用户 ID
  "username": "xiaoluo",     // 用户名
  "type": "USER",            // Token 类型
  "iat": 1779273600,
  "exp": 1779360000          // 根据策略动态计算
}
```

#### Admin Cookie：「X-Admin-Token」
```
Cookie 名称: X-Admin-Token
Cookie 值:   JWT Token（Base64URL 编码）
Cookie 属性: HttpOnly, Secure（生产环境）, SameSite=Lax
有效期:      1 小时（3600 秒）—— ☆ 硬编码不可配置 ☆
             即使修改 config/blog.yml 中的配置也不会生效
JWT Payload:
{
  "sub": "admin_1",          // 管理员 ID
  "username": "admin",       // 管理员用户名
  "type": "ADMIN",           // Token 类型（与用户区分）
  "role": "ADMIN",
  "iat": 1779273600,
  "exp": 1779277200          // 固定 1 小时后过期
}
```

### 插件系统设计

插件系统参考 Minecraft Bukkit 的设计思路喵～
每个插件是一个 JAR 包，根目录包含 `plugin.yml` 声明文件，包内实现 `Plugin` 接口供主程序加载。

#### 插件声明文件 plugin.yml
```yaml
# 喵～ 这是插件的身份证，放在 JAR 包的根目录
# 主程序通过这个文件来识别和加载插件
name: HelloWorld
id: hello-world
version: 1.0.0
author: Xiaoluo
description: 一个简单的示例插件，喵～
main: xl.xldevblog.hello.HelloWorldPlugin  # 插件主类路径
```

#### 插件接口 (Plugin.java)
```java
// 包路径：xl.xldevblog.main.plugin
public interface Plugin {
    // 喵～ 获取插件的唯一标识符，比如 "hello-world"
    String getId();
    // 获取插件的展示名称，显示在后台插件列表里
    String getName();
    // 当前插件版本号，用于检查更新用的
    String getVersion();
    // 插件作者，会显示在后台插件管理页面
    String getAuthor();
    // 插件描述
    String getDescription();
    // 插件被加载时调用，在这里做初始化工作
    void onLoad();
    // 插件被卸载时调用，记得清理资源哟
    void onUnload();
    // 返回插件要注册的路由列表
    List<RouteDefinition> getRoutes();
}
```

#### RouteDefinition 路由定义
```java
// 包路径：xl.xldevblog.main.plugin
public class RouteDefinition {
    private String path;           // 路由路径，如 "/plugin/hello"
    private String method;         // HTTP 方法 GET/POST
    private String viewName;       // 视图名称（可选）
    private String staticPath;     // 静态资源路径（可选）
    private PluginController handler; // 控制器处理器
}
```

#### PluginController 接口
```java
// 插件路由处理器，类似 Spring 的 @Controller
// 包路径：xl.xldevblog.main.plugin
public interface PluginController {
    // 处理 HTTP 请求，返回 ModelAndView 或字符串
    Object handle(Map<String, String> params);
}
```

#### 插件加载流程
1. 系统启动时扫描 `plugins/*.jar`
2. 读取 JAR 根目录的 `plugin.yml`，解析插件元数据
3. 使用自定义 `PluginClassLoader` 加载 JAR（隔离各插件类）
4. 根据 `plugin.yml` 的 `main` 字段实例化插件主类
5. 调用 `onLoad()` 方法初始化插件
6. 注册插件提供的路由到 Spring MVC 容器（类似 `@RequestMapping`）
7. 启动文件监听线程，运行时检测新 JAR 自动加载
8. JAR 更新时自动卸载旧版 → 加载新版（hot swap）

#### HelloWorld 示例插件
- **项目位置**: `hello-world-plugin/`（项目根目录下）
- **包名**: `xl.xldevblog.hello`
- **plugin.yml**:
  ```yaml
  name: HelloWorld
  id: hello-world
  version: 1.0.0
  author: Xiaoluo
  description: 喵～ 第一个示例插件
  main: xl.xldevblog.hello.HelloWorldPlugin
  ```
- **路由**: `/plugin/hello` → 返回独立 HTML 页面
- **页面功能**:
  - 显示"Hello World!"标题
  - 包含一个按钮，点击后在浏览器控制台输出 `Hello World from Plugin!`🎉
  - 同时向后端发送请求，后端日志输出 `[HelloWorld] Button clicked!` 📝
- **构建**: 在项目根目录执行 Maven 打包，产物放入 `plugins/` 目录

#### 插件项目目录结构
```
hello-world-plugin/                  # 插件项目（项目根目录下）
├── src/
│   └── main/
│       ├── java/xl/xldevblog/hello/
│       │   ├── HelloWorldPlugin.java    # 插件主类
│       │   └── HelloWorldController.java # 路由处理器
│       └── resources/
│           ├── plugin.yml               # 插件声明文件
│           └── templates/
│               └── hello.html           # 示例页面模板
└── pom.xml                             # 独立的 Maven 配置

### 前端技术栈

| 模块 | 技术 | 说明 |
|------|------|------|
| 框架 | Vue 3 | 渐进式 JavaScript 框架 |
| 语言 | TypeScript | 类型安全 |
| 构建 | Webpack | 模块打包 |
| UI | TailwindCSS 3 | 原子化 CSS |
| 图标 | Lucide Icons | 简洁图标库 |
| 编辑器 | Vditor | Markdown + WYSIWYG |

#### 前端页面结构
```
├── index.html          # 主页模板
├── admin.html          # 后台入口
├── login.html          # 登录/注册页
├── post.html           # 文章详情模板
└── assets/
    ├── js/
    │   ├── main.js     # 主页脚本
    │   ├── admin.js    # 后台脚本
    │   └── post.js     # 文章脚本
    └── css/
        ├── main.css    # 主页样式
        └── admin.css   # 后台样式
```

#### 页面功能划分

| 页面 | 功能 | 认证要求 |
|------|------|----------|
| `/` | 博客主页，展示文章列表 | 无 |
| `/login` | 用户登录/注册页 | 无 |
| `/user/profile` | 用户个人中心 | 用户 |
| `/post/{slug}` | 文章详情页 | 无 |
| `/admin` | 后台登录页 | 无 |
| `/admin/dashboard` | 后台首页 | admin |
| `/admin/posts` | 文章管理 | admin |
| `/admin/files` | 文件管理 | admin |
| `/admin/plugins` | 插件管理 | admin |
| `/files/{path}` | 文件浏览器（下载站） | 无 |

### 安全设计

#### 密码哈希配置 (Argon2id)
```java
// 喵～ Argon2id 是目前最安全的密码哈希算法之一
// 这些参数设得越高越安全，但也越吃性能
// 当前配置在安全性和性能间取得了不错的平衡
Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
    .withMemoryAsKB(65536)     // 内存成本：64MB，防止 GPU 并行破解
    .withIterations(3)         // 迭代次数：3 轮
    .withParallelism(4)        // 并行度：4 线程
    .withSalt(salt);           // 随机盐值，每个用户不同
```

#### JWT 配置
- **算法**: HS256
- **密钥**: 从环境变量或配置文件读取
- **用户有效**: 1天/7天（外部 config/blog.yml 可配置）
- **Admin有效**: 1小时（**代码级硬编码常量**，外部配置无法覆盖）

#### CSRF 防护
- 使用 Spring Security CSRF Token
- Cookie 设置 SameSite=Lax（允许从站内链接携带 Cookie）

### 配置文件说明

本项目采用**双层配置体系**：
1. **外部配置**（`config/blog.yml` — YAML 格式，因为主人说不要 properties 喵～）
2. **JAR 内置配置**（`application.properties` — 编译后保留 .properties 格式）

#### 外部配置文件：config/blog.yml
```yaml
# ===== 博客系统外部配置 =====
# 喵～ 这个文件放在项目根目录的 config/ 文件夹下
# 修改后重启应用才会生效哟

server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xiaoluoDev_blog_db?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 35433123f
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

# 插件系统配置喵～
plugin:
  directory: ./plugins
  scan-interval: 5000

# 文件存储配置
file:
  upload-dir: ./uploads
  download-dir: ./downloads

# 用户 Cookie 策略
# 注意：Admin Cookie 策略是代码级硬编码的 1 小时，
#       这里改了也对 admin 没用哟 (｀・ω・´)
cookie:
  user:
    default-expiration: 86400        # 默认过期时间（秒）= 1 天
    remember-me-expiration: 604800   # "记住我"过期时间（秒）= 7 天
    cookie-name: X-User-Token
  admin:
    # admin 的 cookie 策略在 Java 代码里写死了喵
    # 别在这里改啦，改代码去～
    cookie-name: X-Admin-Token
```

#### JAR 内置配置文件：application.properties
```properties
# 喵～ 这是编译到 JAR 内部的配置文件
# 外部 config/blog.yml 的配置会覆盖这里的同名配置
# 所以改配置去外面的 yml 文件改就好啦

server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/xiaoluoDev_blog_db?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=35433123f
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 部署说明

#### 开发环境
```bash
# 喵～ 先在 MySQL 里创建数据库（如果还没有的话）
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS xiaoluoDev_blog_db DEFAULT CHARACTER SET utf8mb4;"

# 进入后端目录
cd backend

# 编译打包
mvn clean package -DskipTests

# 运行（会加载外部 config/blog.yml）
java -jar target/blog-1.0.0.jar --spring.config.additional-location=file:../config/blog.yml
```

#### 前端编译
```bash
cd frontend
npm install
npm run build
```

#### 插件部署
```bash
# 把插件 JAR 扔进 plugins/ 目录
cp HelloWorld-1.0.0.jar ../plugins/
# 系统会自动检测并加载，喵～
```

---

## 验收标准总结

| 功能模块 | 验收要点 | 验证方式 |
|----------|----------|----------|
| 用户注册 | 注册成功、密码 Argon2id 哈希 | programmatic |
| 用户登录 | 1天默认 / 勾选记住我=7天 | programmatic |
| Admin 登录 | 1小时硬编码、改配置文件无效 | programmatic |
| Cookie 跨 IP | 换 IP 不掉登录 | human-judgment |
| 管理员唯一 | 只能有一个管理员 | programmatic |
| 插件系统 | JAR 热加载、HelloWorld 插件 | programmatic |
| 文章管理 | CRUD、Vditor 编辑器 | human-judgment |
| 文件管理 | 文件夹创建、文件上传、跨平台路径 | programmatic |
| 下载站 | 附件链接、文件浏览器 | human-judgment |
| 界面设计 | 极简风格、响应式、可读性 | human-judgment |