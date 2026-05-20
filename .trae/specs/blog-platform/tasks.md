# 个人博客 + 下载站平台 - 实现计划

## [ ] P0 Task 1: 初始化 Spring Boot 项目结构
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建 Maven 项目结构（group: xl.xldevblog, artifact: blog, package: xl.xldevblog.main）
  - 配置 pom.xml 依赖（Spring Boot 3.x, Spring Security, Spring Data JPA, MySQL, jjwt, argon2-jvm, Vditor）
  - 创建 JAR 内置配置文件 src/main/resources/application.properties
  - 创建外部配置目录 config/blog.yml（YAML 格式，非 properties）
  - 创建项目目录结构（backend/, frontend/, plugins/, downloads/, uploads/）
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-7, AC-12]
- **Test Requirements**:
  - `programmatic` TR-1.1: Maven 项目能正常编译
  - `programmatic` TR-1.2: Spring Boot 应用能正常启动
  - `programmatic` TR-1.3: 外部 config/blog.yml 存在且为 YAML 格式
- **Notes**: 外部配置用 yml，JAR 内部配置用 properties

## \[ ] P0 Task 2: 数据库初始化与实体类

* **Priority**: P0

* **Depends On**: Task 1

* **Description**:

  * 创建统一 User 实体（is\_admin 字段区分普通用户/管理员，默认管理员唯一）

  * 创建 Post 实体（关联 author\_id）

  * 创建 FileAttachment 实体（支持 parent\_id 文件夹树结构）

  * 创建 PluginInfo 实体

  * 创建对应的 JPA Repository 接口

  * 实现 DataInitializer：首次启动创建默认管理员（用户名 admin，密码 Argon2id 哈希）

  * 数据库账号 root，密码 35433123f，库名 blog\_db

* **Acceptance Criteria Addressed**: \[AC-1, AC-2, AC-12]

* **Test Requirements**:

  * `programmatic` TR-2.1: 数据库表自动创建成功（user, post, file\_attachment, plugin\_info）

  * `programmatic` TR-2.2: 默认管理员账号存在且 is\_admin=true

  * `programmatic` TR-2.3: 默认管理员密码为 Argon2id 哈希（非明文）

  * `programmatic` TR-2.4: 重复初始化不会创建第二个管理员

## \[ ] P0 Task 3: Argon2id 密码哈希服务

* **Priority**: P0

* **Depends On**: Task 2

* **Description**:

  * 实现 Argon2PasswordEncoder 配置（内存成本 65536KB，迭代 3 次，并行度 4）

  * 提供密码加密和验证的方法

  * 在注册/修改密码时自动哈希存储

* **Acceptance Criteria Addressed**: \[AC-1, AC-7]

* **Test Requirements**:

  * `programmatic` TR-3.1: 同一密码每次哈希结果不同（有随机盐）

  * `programmatic` TR-3.2: 密码验证能正确匹配

## \[ ] P0 Task 4: 用户认证系统（1天/7天 Cookie）

* **Priority**: P0

* **Depends On**: Task 2, Task 3

* **Description**:

  * 实现用户注册 API（POST /api/auth/register）

  * 实现用户登录 API（POST /api/auth/login，支持 rememberMe 参数）

  * 实现用户登出 API（POST /api/auth/logout）

  * 实现获取当前用户 API（GET /api/auth/me）

  * 用户 Cookie 名称：X-User-Token

  * 未勾选"记住我"：有效期 1 天

  * 勾选"记住我"：有效期 7 天（可在 config/blog.yml 中配置）

  * Cookie 属性：HttpOnly, Secure（生产环境）, SameSite=Lax

  * JWT Payload 包含：sub, username, type: "USER", iat, exp

* **Acceptance Criteria Addressed**: \[AC-1, AC-2, AC-3, AC-6, AC-7]

* **Test Requirements**:

  * `programmatic` TR-4.1: POST /api/auth/register 创建用户成功

  * `programmatic` TR-4.2: POST /api/auth/login 返回 200 并设置 X-User-Token Cookie

  * `programmatic` TR-4.3: 不传 rememberMe 时 Cookie Max-Age=86400（1天）

  * `programmatic` TR-4.4: 传 rememberMe=true 时 Cookie Max-Age=604800（7天）

  * `human-judgment` TR-4.5: Cookie 包含 HttpOnly 属性

## \[ ] P0 Task 5: Admin 认证系统（1小时硬编码 Cookie）

* **Priority**: P0

* **Depends On**: Task 2, Task 3

* **Description**:

  * 实现管理员登录 API（POST /api/auth/admin/login）

  * 实现管理员登出 API（POST /api/auth/admin/logout）

  * 实现获取管理员信息 API（GET /api/auth/admin/me）

  * Admin Cookie 名称：X-Admin-Token

  * 有效期：**1 小时（3600 秒）—— 代码级硬编码常量**，外部配置无法修改

  * JWT Payload 包含：sub, username, type: "ADMIN", role: "ADMIN", iat, exp

* **Acceptance Criteria Addressed**: \[AC-4, AC-5, AC-6, AC-7]

* **Test Requirements**:

  * `programmatic` TR-5.1: POST /api/auth/admin/login 返回 200 并设置 X-Admin-Token

  * `programmatic` TR-5.2: Admin Cookie Max-Age=3600（1小时）

  * `programmatic` TR-5.3: 修改 config/blog.yml 中的 cookie.user 配置不影响 admin Cookie

  * `programmatic` TR-5.4: 普通用户无法通过 /api/auth/admin/login 登录

## \[ ] P0 Task 6: Spring Security 双重过滤链配置

* **Priority**: P0

* **Depends On**: Task 4, Task 5

* **Description**:

  * 配置 Spring Security 安全过滤链

  * 实现 UserJwtAuthFilter（解析 X-User-Token）

  * 实现 AdminJwtAuthFilter（解析 X-Admin-Token）

  * 两套过滤器分别处理不同的路径前缀

  * 配置公开访问路径（/, /api/posts, /api/files, /login, /register）

  * 配置用户认证路径（/api/auth/*, /user/*）

  * 配置管理员认证路径（/admin/*, /api/admin/*, /api/plugins/\*）

  * 配置 CSRF 防护

  * 配置 SameSite=Lax

* **Acceptance Criteria Addressed**: \[AC-4, AC-6]

* **Test Requirements**:

  * `programmatic` TR-6.1: 未登录访问 /admin 跳转登录页

  * `programmatic` TR-6.2: 管理员登录后可访问 /admin/dashboard

  * `programmatic` TR-6.3: 普通用户无法访问 /admin/\*（返回 403）

  * `programmatic` TR-6.4: 未认证可访问 /api/posts

  * `programmatic` TR-6.5: 未认证不可访问 /api/admin/config

## \[ ] P1 Task 7: 插件系统核心

* **Priority**: P1

* **Depends On**: Task 1

* **Description**:

  * 创建 Plugin 接口（id, name, version, author, description, onLoad, onUnload, getRoutes）

  * 创建 RouteDefinition 类（path, method, handler, viewName, staticPath）

  * 创建 PluginController 接口（类似 Spring @Controller，处理插件 HTTP 请求）

  * 创建 PluginClassLoader（自定义 URLClassLoader，隔离各插件类）

  * 创建 PluginManager（扫描 plugins/\*.jar，读取 plugin.yml，热加载，文件监听）

  * PluginManager 根据 plugin.yml 的 main 字段定位插件主类并实例化

  * 插件信息持久化到 plugin\_info 表

* **Acceptance Criteria Addressed**: \[AC-8]

* **Test Requirements**:

  * `programmatic` TR-7.1: plugins/ 目录 JAR 文件能被扫描发现

  * `programmatic` TR-7.2: JAR 内 plugin.yml 能被正确解析

  * `programmatic` TR-7.3: 根据 plugin.yml 的 main 字段加载插件类并实例化

  * `programmatic` TR-7.4: 插件信息写入 plugin\_info 表

## \[ ] P1 Task 8: 文章管理 API

* **Priority**: P1

* **Depends On**: Task 2, Task 6

* **Description**:

  * 实现文章 CRUD API

  * 文章列表分页查询（GET /api/posts?page=0\&size=10）

  * 文章按 slug 查询（GET /api/posts/{slug}）

  * 创建/更新/删除需管理员认证

* **Acceptance Criteria Addressed**: \[AC-10]

* **Test Requirements**:

  * `programmatic` TR-8.1: GET /api/posts 返回分页文章列表

  * `programmatic` TR-8.2: POST /api/posts 创建文章成功（需 admin 认证）

  * `programmatic` TR-8.3: GET /api/posts/{slug} 返回单篇文章

  * `programmatic` TR-8.4: PUT /api/posts/{id} 更新文章成功

  * `programmatic` TR-8.5: DELETE /api/posts/{id} 删除文章成功

## [ ] P1 Task 9: 文件管理 API（隐性下载站）
- **Priority**: P1
- **Depends On**: Task 2, Task 6
- **Description**:
  - 获取文件树（GET /api/files，返回根目录内容）
  - 获取指定文件夹内容（GET /api/files/{parentId}）
  - 上传文件（POST /api/files/upload，需 admin 认证）
  - 创建文件夹（POST /api/files/folder，需 admin 认证）
  - 下载文件（GET /api/files/download/{id}，流式下载）
  - 删除文件/文件夹（DELETE /api/files/{id}，需 admin 认证）
  - 支持 Windows 盘符路径（如 D:\files）和 Linux 绝对路径（如 /opt/files）
  - ⚠️ **路径穿越防护**：所有文件操作必须使用 `File.getCanonicalPath()` 校验路径合法性
- **Acceptance Criteria Addressed**: [AC-9, AC-11]
- **Test Requirements**:
  - `programmatic` TR-9.1: GET /api/files 返回文件列表
  - `programmatic` TR-9.2: POST /api/files/folder 创建文件夹成功
  - `programmatic` TR-9.3: POST /api/files/upload 上传文件成功
  - `programmatic` TR-9.4: GET /api/files/download/{id} 下载文件成功
  - `programmatic` TR-9.5: 支持 Windows 路径格式
  - `programmatic` TR-9.6: 支持 Linux 路径格式
  - `programmatic` TR-9.7: 路径包含 `../` 时被拒绝（返回 403）
  - `programmatic` TR-9.8: 路径包含 `~` 时被拒绝
  - `programmatic` TR-9.9: 路径解析后超出 uploads/ 或 downloads/ 目录时被拒绝

## [ ] P1 Task 10: HelloWorld 示例插件

* **Priority**: P1

* **Depends On**: Task 7

* **Description**:

  * 在项目根目录创建独立的 Maven 插件模块 `hello-world-plugin/`
  * 包名：xl.xldevblog.hello
  * 在 JAR 根目录创建 plugin.yml 声明插件信息（name, id, version, author, description, main）
  * 实现 Plugin 接口（id=hello-world, name=HelloWorld Plugin, version=1.0.0）
  * 实现 PluginController 接口处理 HTTP 请求
  * 注册 /plugin/hello 路由，返回独立 HTML 页面
  * 页面包含一个按钮，点击后：
    - 浏览器控制台输出 `Hello World from Plugin!`
    - 向后端 /plugin/hello/click 发送请求，后端日志输出 `[HelloWorld] Button clicked!`
  * 将插件打包为 JAR 放入 plugins/ 目录

* **Acceptance Criteria Addressed**: [AC-8]

* **Test Requirements**:

  * `programmatic` TR-10.1: 插件 JAR 放入 plugins/ 后自动加载
  * `programmatic` TR-10.2: 访问 /plugin/hello 返回 200 和 HTML 页面
  * `programmatic` TR-10.3: 点击按钮后后端日志出现 `[HelloWorld] Button clicked!`
  * `human-judgment` TR-10.4: 点击按钮后浏览器控制台输出 `Hello World from Plugin!`

## \[ ] P1 Task 11: 插件路由注册

* **Priority**: P1

* **Depends On**: Task 7, Task 10

* **Description**:

  * 实现插件路由动态注册到 Spring MVC

  * 支持插件提供静态资源

  * 支持插件提供视图页面

  * 插件卸载时自动移除路由

* **Acceptance Criteria Addressed**: \[AC-8]

* **Test Requirements**:

  * `programmatic` TR-11.1: 插件路由能正确注册到 Spring 容器

  * `programmatic` TR-11.2: 卸载插件时路由自动移除

## \[ ] P2 Task 12: 前端页面 - 博客主页

* **Priority**: P2

* **Depends On**: Task 8

* **Description**:

  * 创建主页 HTML 模板（极简风格）

  * Vue3 + TypeScript 组件

  * 文章列表展示（标题、摘要、日期、标签、作者）

  * 分页功能

  * 响应式设计

  * 导航栏（主页、登录/注册）

* **Acceptance Criteria Addressed**: \[FR-1]

* **Test Requirements**:

  * `human-judgment` TR-12.1: 页面美观简洁，符合极简风格

  * `human-judgment` TR-12.2: 文章列表正常展示

  * `human-judgment` TR-12.3: 移动端适配良好

## \[ ] P2 Task 13: 前端页面 - 用户登录/注册页

* **Priority**: P2

* **Depends On**: Task 4

* **Description**:

  * 创建登录/注册 HTML 模板

  * 登录表单（用户名 + 密码 + "记住我"复选框）

  * 注册表单（用户名 + 密码 + 邮箱）

  * 切换登录/注册模式

  * 表单校验

* **Acceptance Criteria Addressed**: \[FR-4]

* **Test Requirements**:

  * `human-judgment` TR-13.1: 登录/注册表单正常显示

  * `human-judgment` TR-13.2: "记住我"复选框正常工作

  * `human-judgment` TR-13.3: 表单校验提示友好

## \[ ] P2 Task 14: 前端页面 - 文章详情页 + 文件浏览器

* **Priority**: P2

* **Depends On**: Task 8, Task 9

* **Description**:

  * 创建文章详情 HTML 模板

  * Markdown 内容渲染（marked.js 或类似库）

  * 代码高亮（highlight.js）

  * 附件列表展示（点击跳转文件浏览器）

  * 文件浏览器页面（类似 openlist 风格）

  * 文件夹树展示，点击进入子文件夹

  * 文件下载按钮

* **Acceptance Criteria Addressed**: \[FR-2, FR-6, AC-9]

* **Test Requirements**:

  * `human-judgment` TR-14.1: Markdown 内容正确渲染

  * `human-judgment` TR-14.2: 代码高亮正常

  * `human-judgment` TR-14.3: 附件列表正常显示

  * `human-judgment` TR-14.4: 文件浏览器文件夹树正常展示

  * `human-judgment` TR-14.5: 点击进入子文件夹正常

  * `human-judgment` TR-14.6: 文件下载功能正常

## \[ ] P2 Task 15: 前端页面 - Admin 后台

* **Priority**: P2

* **Depends On**: Task 5, Task 8, Task 9, Task 10

* **Description**:

  * 创建 admin.html 模板（SPA 风格后台）

  * 管理员登录页（使用 X-Admin-Token）

  * 后台首页（dashboard，显示统计数据）

  * 文章管理页（列表 + Vditor 编辑器）

  * 文件管理页（目录树 + 上传/新建文件夹）

  * 插件管理页（已加载插件列表）

  * Cookie 策略配置页（用户侧的 1天/7天阈值可调）

* **Acceptance Criteria Addressed**: \[FR-3, AC-10]

* **Test Requirements**:

  * `human-judgment` TR-15.1: 管理员登录页面正常工作

  * `human-judgment` TR-15.2: 文章编辑器支持 Markdown + WYSIWYG

  * `human-judgment` TR-15.3: 文件管理支持创建文件夹和上传

  * `human-judgment` TR-15.4: 插件列表正常展示

  * `human-judgment` TR-15.5: Cookie 策略配置页面可用

## \[ ] P2 Task 16: 后台配置 API + 双层配置加载

* **Priority**: P2

* **Depends On**: Task 1, Task 5

* **Description**:

  * 实现配置获取 API（GET /api/admin/config）

  * 实现配置更新 API（PUT /api/admin/config）

  * 外部 config/blog.yml 加载逻辑

  * JAR 内置 application.properties 作为默认值

  * 外部配置覆盖内部配置

* **Acceptance Criteria Addressed**: \[FR-3, NFR-1]

* **Test Requirements**:

  * `programmatic` TR-16.1: 外部 config/blog.yml 配置优先于内置配置

  * `programmatic` TR-16.2: JAR 内置配置保持 .properties 格式

  * `programmatic` TR-16.3: 配置更新 API 保存到外部 config/blog.yml

## \[ ] P2 Task 17: 部署配置与优化

* **Priority**: P2

* **Depends On**: All

* **Description**:

  * 配置静态资源缓存

  * 优化数据库连接池（HikariCP）

  * 创建启动脚本（start.bat / start.sh）

  * 配置跨域处理（CORS）

  * 添加 .gitignore

* **Acceptance Criteria Addressed**: \[NFR-2]

* **Test Requirements**:

  * `programmatic` TR-17.1: 首页加载时间 < 2 秒

  * `human-judgment` TR-17.2: 启动脚本正常工作

## \[ ] P2 Task 18: 代码整理与文档

* **Priority**: P2

* **Depends On**: All

* **Description**:

  * 整理代码结构

  * 添加详细注释（轻猫娘语风）

  * 更新 README.md（启动说明）

  * 创建 API 文档

* **Test Requirements**:

  * `human-judgment` TR-18.1: 代码结构清晰

  * `human-judgment` TR-18.2: 注释风格统一，有猫娘语风

  * `human-judgment` TR-18.3: README 包含启动说明

## \[ ] P2 Task 19: 推送至 GitHub

* **Priority**: P2

* **Depends On**: All

* **Description**:

  * 初始化 Git 仓库

  * 添加 .gitignore（排除 target/, node\_modules/, uploads/, downloads/）

  * 提交代码

  * 推送到 GitHub（<https://github.com/xiaoluo2333/xiaoluoDev-blog1）>

* **Test Requirements**:

  * `programmatic` TR-19.1: 代码成功推送到 GitHub

***

## 任务依赖关系图

```
Task 1 (项目初始化)
    ├── Task 2 (数据库实体)
    │       ├── Task 3 (Argon2id服务)
    │       │       ├── Task 4 (用户认证 - 1天/7天)
    │       │       │       ├── Task 6 (Security双重过滤链)
    │       │       │       │       ├── Task 8 (文章API)
    │       │       │       │       │       ├── Task 12 (主页前端)
    │       │       │       │       │       └── Task 14 (文章详情+文件浏览器)
    │       │       │       │       ├── Task 9 (文件API)
    │       │       │       │       │       └── Task 14 (文章详情+文件浏览器)
    │       │       │       │       └── Task 13 (用户登录注册页)
    │       │       │       └── Task 5 (Admin认证 - 1小时硬编码)
    │       │       │               └── Task 6
    │       │       │                       └── Task 15 (Admin后台)
    │       │       └── Task 16 (配置API + 双层配置)
    │       └── Task 7 (插件系统核心)
    │               ├── Task 10 (HelloWorld插件)
    │               └── Task 11 (插件路由注册)
    └── Task 17 (部署优化)
    └── Task 18 (文档)
    └── Task 19 (GitHub)
```

***

## 预计时间估算

| 任务      | 预计工时    | 说明             |
| ------- | ------- | -------------- |
| Task 1  | 1h      | 项目初始化          |
| Task 2  | 2h      | 数据库实体          |
| Task 3  | 0.5h    | Argon2id 服务    |
| Task 4  | 3h      | 用户认证系统         |
| Task 5  | 2h      | Admin 认证系统     |
| Task 6  | 2h      | Security 双重过滤链 |
| Task 7  | 4h      | 插件系统核心         |
| Task 8  | 3h      | 文章 API         |
| Task 9  | 4h      | 文件管理 API       |
| Task 10 | 2h      | HelloWorld 插件  |
| Task 11 | 2h      | 插件路由注册         |
| Task 12 | 3h      | 博客主页前端         |
| Task 13 | 2h      | 登录/注册页         |
| Task 14 | 3h      | 文章详情 + 文件浏览器   |
| Task 15 | 5h      | Admin 后台       |
| Task 16 | 1h      | 后台配置 API       |
| Task 17 | 1h      | 部署优化           |
| Task 18 | 1h      | 文档整理           |
| Task 19 | 0.5h    | GitHub 推送      |
| **总计**  | **41h** | <br />         |

