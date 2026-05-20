# 个人博客 + 下载站平台 - 验证清单

## 一、项目结构验证
- [ ] 后端 Maven 项目结构完整（controller, service, repository, entity, dto, config, security, plugin）
- [ ] 前端项目结构完整（src/components, src/views, src/api）
- [ ] 外部配置文件存在 config/blog.yml 且为 YAML 格式
- [ ] JAR 内部配置文件为 application.properties 格式
- [ ] 插件目录存在（plugins/）
- [ ] 文件存储目录存在（uploads/, downloads/）
- [ ] .gitignore 文件存在

## 二、数据库验证
- [ ] 数据库连接正常（root / 35433123f，库 xiaoluoDev_blog_db）
- [ ] 表结构自动创建成功（user, post, file_attachment, plugin_info）
- [ ] user 表包含 is_admin 字段区分普通用户/管理员
- [ ] 默认管理员账号存在（admin，密码 Argon2id 哈希）
- [ ] 重复初始化不会创建第二个管理员

## 三、Argon2id 密码哈希验证
- [ ] 同一密码每次哈希结果不同（有随机盐）
- [ ] 密码验证能正确匹配
- [ ] 哈希参数：内存 65536KB，迭代 3 次，并行度 4

## 四、用户认证系统验证（1天/7天 Cookie）
- [ ] POST /api/auth/register 创建用户成功
- [ ] POST /api/auth/login 返回 200 并设置 X-User-Token Cookie
- [ ] 未勾选"记住我"：Cookie Max-Age=86400（1天）
- [ ] 勾选"记住我"：Cookie Max-Age=604800（7天）
- [ ] Cookie 包含 HttpOnly 属性
- [ ] Cookie 包含 SameSite=Lax 属性
- [ ] POST /api/auth/logout 清除 Cookie
- [ ] GET /api/auth/me 返回当前用户信息
- [ ] 更换 IP 后登录状态保持

## 五、Admin 认证系统验证（1小时硬编码）
- [ ] POST /api/auth/admin/login 返回 200 并设置 X-Admin-Token
- [ ] Admin Cookie Max-Age=3600（1小时）
- [ ] 修改 config/blog.yml 配置不影响 Admin Cookie 有效期
- [ ] 普通用户无法通过 /api/auth/admin/login 登录
- [ ] 管理员唯一（is_admin=true 的唯一用户）
- [ ] GET /api/auth/admin/me 返回管理员信息

## 六、Spring Security 双重过滤链验证
- [ ] 未登录访问 /admin 自动跳转登录页
- [ ] 管理员登录后可正常访问 /admin/dashboard
- [ ] 普通用户无法访问 /admin/*（返回 403）
- [ ] 未认证可访问 /api/posts（文章列表）
- [ ] 未认证不可访问 /api/admin/config
- [ ] CSRF 防护配置正确

## 七、插件系统验证
- [ ] 插件目录（plugins/）JAR 文件能被扫描发现
- [ ] JAR 内 plugin.yml 能被正确解析
- [ ] 根据 plugin.yml 的 main 字段加载插件类并实例化
- [ ] 插件信息持久化到 plugin_info 表
- [ ] HelloWorld 插件能自动加载
- [ ] 访问 /plugin/hello 返回 HTML 页面
- [ ] 插件路由动态注册成功（Spring MVC 容器）
- [ ] 插件卸载时路由自动移除
- [ ] PluginController 接口能正常处理插件 HTTP 请求

## 八、文章管理 API 验证
- [ ] GET /api/posts 返回分页文章列表
- [ ] GET /api/posts/{slug} 返回单篇文章
- [ ] POST /api/posts 创建文章成功（需 admin 认证）
- [ ] PUT /api/posts/{id} 更新文章成功（需 admin 认证）
- [ ] DELETE /api/posts/{id} 删除文章成功（需 admin 认证）

## 九、文件管理 API 验证（隐性下载站）
- [ ] GET /api/files 返回文件列表
- [ ] GET /api/files/{parentId} 返回指定文件夹内容
- [ ] POST /api/files/upload 上传文件成功（需 admin 认证）
- [ ] POST /api/files/folder 创建文件夹成功（需 admin 认证）
- [ ] DELETE /api/files/{id} 删除文件/文件夹成功（需 admin 认证）
- [ ] GET /api/files/download/{id} 流式下载文件
- [ ] 支持 Windows 路径格式（如 D:\files）
- [ ] 支持 Linux 路径格式（如 /opt/files）
- [ ] ⚠️ **路径穿越防护**：路径包含 `../` 时被拒绝（返回 403）
- [ ] ⚠️ **路径穿越防护**：路径包含 `~` 时被拒绝
- [ ] ⚠️ **路径穿越防护**：路径解析后超出 uploads/ 或 downloads/ 目录时被拒绝
- [ ] ⚠️ **路径穿越防护**：使用 `File.getCanonicalPath()` 校验

## 十、前端页面验证

### 博客主页
- [ ] 页面美观简洁，符合极简风格
- [ ] 文章列表正常展示（标题、摘要、日期、标签、作者）
- [ ] 分页功能正常
- [ ] 响应式设计（移动端适配良好）
- [ ] 导航栏正常工作（主页、登录/注册链接）

### 用户登录/注册页
- [ ] 登录表单正常显示（用户名 + 密码 + "记住我"复选框）
- [ ] 注册表单正常显示（用户名 + 密码 + 邮箱）
- [ ] 登录/注册模式切换正常
- [ ] 表单校验提示友好
- [ ] "记住我"复选框正常工作

### 文章详情页
- [ ] Markdown 内容正确渲染
- [ ] 代码高亮正常显示
- [ ] 附件列表正常显示
- [ ] 点击附件跳转到文件浏览器

### 文件浏览器（下载站）
- [ ] 文件夹树正常展示（类似 openlist）
- [ ] 点击进入子文件夹正常
- [ ] 返回上级目录正常
- [ ] 文件下载功能正常

### Admin 后台
- [ ] 管理员登录页面正常工作（X-Admin-Token）
- [ ] 后台首页（dashboard）正常显示
- [ ] 文章管理页面 CRUD 功能正常
- [ ] Vditor 编辑器支持 Markdown + WYSIWYG
- [ ] 文件管理支持创建文件夹和上传
- [ ] 插件列表正常展示
- [ ] Cookie 策略配置页面可用

## 十一、后台配置 API 验证
- [ ] GET /api/admin/config 返回系统配置
- [ ] PUT /api/admin/config 更新配置成功（需 admin 认证）
- [ ] 外部 config/blog.yml 配置优先于内置配置
- [ ] JAR 内置配置保持 .properties 格式

## 十二、性能与安全验证
- [ ] 首页加载时间 < 2 秒
- [ ] 静态资源缓存配置正确
- [ ] 数据库连接池配置合理（HikariCP）
- [ ] 跨域配置正确（CORS）
- [ ] Cookie 属性 HttpOnly 和 SameSite 正确设置

## 十三、部署验证
- [ ] Maven 项目能正常编译（mvn clean package）
- [ ] Spring Boot 应用能正常启动
- [ ] 前端项目能正常编译（npm run build）
- [ ] 启动脚本正常工作（start.bat / start.sh）
- [ ] 代码成功推送到 GitHub

## 十四、HelloWorld 示例插件验证
- [ ] 插件项目位于项目根目录 hello-world-plugin/ 下
- [ ] 包名正确（xl.xldevblog.hello）
- [ ] JAR 根目录包含 plugin.yml 声明文件
- [ ] plugin.yml 包含 name, id, version, author, description, main 字段
- [ ] main 指向正确的插件主类（xl.xldevblog.hello.HelloWorldPlugin）
- [ ] 实现 Plugin 接口
- [ ] 实现 PluginController 接口处理 HTTP 请求
- [ ] 注册 /plugin/hello 路由
- [ ] 提供独立 HTML 页面包含"Hello World!"标题
- [ ] 页面包含一个按钮
- [ ] 点击按钮后浏览器控制台输出 `Hello World from Plugin!`
- [ ] 点击按钮向后端发送请求，后端日志输出 `[HelloWorld] Button clicked!`
- [ ] 热加载功能正常（运行中放入 plugins/ 自动加载）

## 十五、注释规范验证
- [ ] 所有类、方法、关键逻辑包含详细注释
- [ ] 注释解释"为什么这样做"而非仅仅"做了什么"
- [ ] 注释风格一致，轻猫娘语风