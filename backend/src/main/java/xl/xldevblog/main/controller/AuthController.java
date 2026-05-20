package xl.xldevblog.main.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xl.xldevblog.main.dto.ApiResponse;
import xl.xldevblog.main.dto.LoginRequest;
import xl.xldevblog.main.dto.LoginResponse;
import xl.xldevblog.main.dto.RegisterRequest;
import xl.xldevblog.main.entity.User;
import xl.xldevblog.main.security.JwtUtil;
import xl.xldevblog.main.service.AuthService;

import java.util.Map;

/**
 * 喵～ 认证控制器！
 * <p>
 * 处理用户注册、登录、登出、获取当前用户信息，
 * 以及管理员登录、登出、获取当前管理员信息等接口。
 * <p>
 * Token 全部通过 HttpOnly Cookie 传递，
 * 增强了 XSS 防护能力，安全喵！(๑•̀ㅂ•́)و✧
 * <p>
 * 接口前缀统一为 /api/auth，
 * 管理员相关的接口在 /api/auth/admin 下喵～
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /** 用户 Token 的 Cookie 名称 */
    private static final String USER_TOKEN_COOKIE = "X-User-Token";

    /** 管理员 Token 的 Cookie 名称 */
    private static final String ADMIN_TOKEN_COOKIE = "X-Admin-Token";

    /** 认证服务，处理登录注册等业务逻辑 */
    private final AuthService authService;

    /** JWT Token 工具类，用来解析 token 获取用户信息 */
    private final JwtUtil jwtUtil;

    /**
     * 构造注入认证服务和 JWT 工具类
     *
     * @param authService 认证服务
     * @param jwtUtil     JWT Token 工具类
     */
    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 喵～ 创建 HttpOnly Cookie 的工具方法！
     * <p>
     * 统一配置 Cookie 的属性：
     * <ul>
     *   <li>HttpOnly = true — 不让 JavaScript 读取，防止 XSS 攻击</li>
     *   <li>SameSite = Lax — 允许在同站请求中携带，同时防止 CSRF</li>
     *   <li>Path = / — 全站有效</li>
     * </ul>
     *
     * @param name     Cookie 名称
     * @param value    Cookie 值（token）
     * @param maxAge   过期时间（秒），负数表示浏览器会话结束即过期
     * @return 配置好的 Cookie 对象
     */
    private Cookie createAuthCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Lax");
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    /**
     * POST /api/auth/register — 用户注册
     * <p>
     * 注册一个新用户，用户名和邮箱不能重复喵～
     *
     * @param req 注册请求体（username, password, email）
     * @return 注册结果
     */
    @PostMapping("/register")
    public ApiResponse register(@RequestBody RegisterRequest req) {
        try {
            User user = authService.register(req);
            log.info("用户注册成功：{}", user.getUsername());
            return ApiResponse.ok("注册成功喵～欢迎加入的说！(ฅ´ω`ฅ)");
        } catch (IllegalArgumentException e) {
            log.warn("用户注册失败：{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * POST /api/auth/login — 用户登录
     * <p>
     * 登录成功后会在响应中设置 X-User-Token Cookie，
     * 后续请求浏览器会自动带上这个 Cookie，
     * 不需要前端手动处理 token 喵～
     *
     * @param req      登录请求体（username, password, rememberMe）
     * @param response HTTP 响应，用来设置 Cookie
     * @return 登录结果，包含 token、用户名、过期时间
     */
    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginRequest req, HttpServletResponse response) {
        try {
            LoginResponse loginResponse = authService.login(
                    req.getUsername(), req.getPassword(), req.isRememberMe()
            );

            int maxAge = req.isRememberMe() ? 604800 : 86400;
            Cookie cookie = createAuthCookie(USER_TOKEN_COOKIE, loginResponse.getToken(), maxAge);
            response.addCookie(cookie);

            log.info("用户 {} 登录成功喵～ Cookie 有效期: {} 秒", req.getUsername(), maxAge);
            return ApiResponse.ok(loginResponse);
        } catch (IllegalArgumentException e) {
            log.warn("用户登录失败：{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * POST /api/auth/logout — 用户登出
     * <p>
     * 清除 X-User-Token Cookie（设置 Max-Age=0 让浏览器立即删除），
     * 这样就完成登出啦喵～
     *
     * @param response HTTP 响应，用来清除 Cookie
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ApiResponse logout(HttpServletResponse response) {
        Cookie cookie = createAuthCookie(USER_TOKEN_COOKIE, "", 0);
        response.addCookie(cookie);
        log.info("用户登出成功喵～ Cookie 已清除");
        return ApiResponse.ok("已登出喵～");
    }

    /**
     * GET /api/auth/me — 获取当前登录用户信息
     * <p>
     * 从 Cookie 中读取 X-User-Token，
     * 解析 JWT 获取用户 ID，
     * 然后查询用户信息返回喵～
     * <p>
     * 如果 Cookie 中没有 token 或 token 无效，
     * 会返回错误信息喵。
     *
     * @param token X-User-Token Cookie 的值
     * @return 当前登录用户的信息
     */
    @GetMapping("/me")
    public ApiResponse me(@CookieValue(value = USER_TOKEN_COOKIE, defaultValue = "") String token) {
        if (token.isBlank()) {
            log.warn("获取用户信息失败：Cookie 中没有找到 {} 的说～", USER_TOKEN_COOKIE);
            return ApiResponse.error("未登录喵～请先登录的说！");
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("获取用户信息失败：Token 无效或已过期喵～");
                return ApiResponse.error("Token 已过期或无效，请重新登录喵～");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            Map<String, Object> userInfo = authService.getCurrentUser(userId);
            return ApiResponse.ok(userInfo);
        } catch (IllegalArgumentException e) {
            log.warn("获取用户信息失败：{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * POST /api/auth/admin/login — 管理员登录
     * <p>
     * 管理员专用的登录接口，
     * 只有 isAdmin = true 的用户才能登录成功喵！
     * 登录成功后设置 X-Admin-Token Cookie，有效期为 1 小时。
     *
     * @param req      登录请求体（username, password）
     * @param response HTTP 响应，用来设置 Cookie
     * @return 登录结果，包含 token、用户名、过期时间
     */
    @PostMapping("/admin/login")
    public ApiResponse adminLogin(@RequestBody LoginRequest req, HttpServletResponse response) {
        try {
            LoginResponse loginResponse = authService.adminLogin(
                    req.getUsername(), req.getPassword()
            );

            Cookie cookie = createAuthCookie(ADMIN_TOKEN_COOKIE, loginResponse.getToken(), 3600);
            response.addCookie(cookie);

            log.info("管理员 {} 登录成功喵～", req.getUsername());
            return ApiResponse.ok(loginResponse);
        } catch (IllegalArgumentException e) {
            log.warn("管理员登录失败：{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * POST /api/auth/admin/logout — 管理员登出
     * <p>
     * 清除 X-Admin-Token Cookie，
     * 管理员登出后需要重新登录才能访问管理后台喵～
     *
     * @param response HTTP 响应，用来清除 Cookie
     * @return 登出结果
     */
    @PostMapping("/admin/logout")
    public ApiResponse adminLogout(HttpServletResponse response) {
        Cookie cookie = createAuthCookie(ADMIN_TOKEN_COOKIE, "", 0);
        response.addCookie(cookie);
        log.info("管理员登出成功喵～ Cookie 已清除");
        return ApiResponse.ok("管理员已登出喵～");
    }

    /**
     * GET /api/auth/admin/me — 获取当前管理员信息
     * <p>
     * 从 Cookie 中读取 X-Admin-Token，
     * 解析 JWT 获取管理员 ID，
     * 返回管理员信息。
     * <p>
     * 只有管理员才能通过这个接口获取信息喵～
     *
     * @param token X-Admin-Token Cookie 的值
     * @return 当前管理员的信息
     */
    @GetMapping("/admin/me")
    public ApiResponse adminMe(@CookieValue(value = ADMIN_TOKEN_COOKIE, defaultValue = "") String token) {
        if (token.isBlank()) {
            log.warn("获取管理员信息失败：Cookie 中没有找到 {} 的说～", ADMIN_TOKEN_COOKIE);
            return ApiResponse.error("管理员未登录喵～请先登录的说！");
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("获取管理员信息失败：Token 无效或已过期喵～");
                return ApiResponse.error("Token 已过期或无效，请重新登录喵～");
            }

            Long adminId = jwtUtil.getUserIdFromToken(token);
            Map<String, Object> adminInfo = authService.getCurrentAdmin(adminId);
            return ApiResponse.ok(adminInfo);
        } catch (IllegalArgumentException e) {
            log.warn("获取管理员信息失败：{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}