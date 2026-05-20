package xl.xldevblog.main.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 喵～ 管理员 JWT 认证过滤器！
 * <p>
 * 同样继承 OncePerRequestFilter，
 * 从 Cookie 中提取 X-Admin-Token 并验证，
 * 额外检查 token 的 type 是否为 ADMIN，
 * 确保普通用户的 token 无法通过管理员接口的认证。
 * <p>
 * 只对 /admin/*、/api/admin/*、/api/plugins/* 等路径生效，
 * 其他路径直接跳过处理喵！
 */
@Component
public class AdminJwtAuthFilter extends OncePerRequestFilter {

    /** 管理员 Token 在 Cookie 中的名称 */
    private static final String TOKEN_COOKIE_NAME = "X-Admin-Token";

    /** 需要进行管理员认证的路径前缀列表 */
    private static final List<String> AUTHENTICATED_PATHS = List.of(
            "/admin/",
            "/api/admin/",
            "/api/plugins/",
            "/api/files/"
    );

    private final JwtUtil jwtUtil;

    public AdminJwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 判断当前请求是否需要经过此过滤器
     * <p>
     * 只有请求路径以 /admin/、/api/admin/ 或 /api/plugins/ 开头时，
     * 才需要进行管理员认证喵～
     *
     * @param request HTTP 请求
     * @return true = 跳过此过滤器，false = 执行过滤
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return AUTHENTICATED_PATHS.stream().noneMatch(path::startsWith);
    }

    /**
     * 执行管理员认证过滤逻辑
     * <p>
     * 从 Cookie 中提取 X-Admin-Token，
     * 先验证 token 有效性，再检查 token 类型是否为 ADMIN，
     * 双重验证确保安全性喵！
     * <p>
     * 验证通过后设置 ADMIN 角色的认证信息到 SecurityContextHolder，
     * 后续的 Security 配置会根据角色控制接口访问权限。
     *
     * @param request   HTTP 请求
     * @param response  HTTP 响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = extractTokenFromCookie(request);
            if (token != null
                    && jwtUtil.validateToken(token)
                    && "ADMIN".equals(jwtUtil.getTokenType(token))) {
                Long adminId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                adminId,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // token 解析出错时安静处理
            // 不抛出异常，让过滤器链继续走下去
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求 Cookie 中提取管理员 Token
     * <p>
     * 遍历所有 Cookie，找到名为 X-Admin-Token 的那个，
     * 没找到的话就返回 null 喵～
     *
     * @param request HTTP 请求
     * @return Token 字符串，没找到则返回 null
     */
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}