package xl.xldevblog.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 喵～ 页面控制器！
 * <p>
 * 这个 @Controller（不是 @RestController）负责返回 HTML 模板页面，
 * 所有使用 Thymeleaf + Vue 3 的前端页面都由这里路由喵～
 * <p>
 * RESTful API 请去其他 controller 找的说 (｀・ω・´)
 */
@Controller
public class PageController {

    /**
     * 喵～ 博客主页！
     * <p>
     * 访问 / 或 /index 都会返回 index.html 模板，
     * 里面的 Vue 3 会负责渲染文章列表喵～
     *
     * @return index.html 模板
     */
    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    /**
     * 喵～ 登录/注册页！
     * <p>
     * 访问 /login 返回 login.html，
     * Vue 3 会处理登录/注册的 tab 切换和表单提交喵～
     *
     * @return login.html 模板
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 喵～ 文章详情页！
     * <p>
     * 访问 /post/{slug} 返回 post.html，
     * Vue 3 通过 URL 路径解析 slug 后调用 /api/posts/{slug} 获取文章内容。
     * <p>
     * 注意：这里用 {@code @GetMapping("/post/**")} 捕获所有 /post/ 路径，
     * 而不是用 @PathVariable 捕获 slug，因为页面渲染不依赖后端数据，
     * 所有数据都由前端的 Vue 3 通过 fetch API 获取喵～
     *
     * @return post.html 模板
     */
    @GetMapping("/post/**")
    public String post() {
        return "post";
    }

    /**
     * 喵～ Admin 后台管理页（SPA）！
     * <p>
     * 访问 /admin 返回 admin.html，
     * 这是一个完整的 Vue 3 SPA，
     * 包含 Dashboard、文章管理、文件管理、插件管理四个面板喵～
     * <p>
     * 注意：SecurityConfig 中 /admin/** 需要 ADMIN 角色，
     * 所以这个页面只有登录了管理员账号才能访问的说！
     *
     * @return admin.html 模板
     */
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}