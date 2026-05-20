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
 * 喵～ 用户 JWT 认证过滤器！
 * <p>
 * 继承 OncePerRequestFilter，确保每次请求只过滤一次。
 * 从 Cookie 中提取 X-User-Token 并验证，
 * 路径只对 /api/auth/me 和 /user/* 等需要用户认证的接口生效。
 * <p>
 * 如果 token 无效，不会抛出异常，
 * 而是让过滤器链继续走下去，
 * 由 Spring Security 的后续逻辑来处理未认证的情况喵～
 */
@Component
public class UserJwtAuthFilter extends OncePerRequestFilter {

    /** 用户 Token 在 Cookie 中的名称 */
    private static final String TOKEN_COOKIE_NAME = "X-User-Token";

    /** 需要进行用户认证的路径前缀列表 */
    private static final List<String> AUTHENTICATED_PATHS = List.of(
            "/api/auth/me",
            "/user/"
    );

    private final JwtUtil jwtUtil;

    public UserJwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 判断当前请求是否需要经过此过滤器
     * <p>
     * 只有请求路径匹配用户认证路径时才会进行过滤，
     * 其他路径直接跳过，减少不必要的性能开销喵～
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
     * 执行过滤逻辑
     * <p>
     * 从 Cookie 中提取 X-User-Token，
     * 用 JwtUtil 验证 token 有效性，
     * 验证通过后将认证信息设置到 SecurityContextHolder 中，
     * 后续的 Controller 就可以通过 SecurityContext 获取当前用户信息啦！
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
            if (token != null && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // token 解析出错，不抛出异常
            // 让过滤器链继续走下去，Spring Security 会处理未认证的情况
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求 Cookie 中提取 Token
     * <p>
     * 遍历所有 Cookie，找到名为 X-User-Token 的那个，
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