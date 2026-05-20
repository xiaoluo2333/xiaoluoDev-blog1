package xl.xldevblog.main.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import xl.xldevblog.main.config.Argon2PasswordEncoder;

import java.util.List;

/**
 * 喵～ Spring Security 主配置类！
 * <p>
 * 整个应用的安全配置都在这里啦。
 * 因为咱们用的是 JWT + Cookie 的认证方式，
 * 所以不需要 CSRF 保护，也不需要 session。
 * <p>
 * 安全策略总结：
 * <ul>
 *   <li>公开接口（登录、注册、文章列表等）无需认证</li>
 *   <li>用户接口（/user/**、/api/auth/me）需要用户登录</li>
 *   <li>管理员接口（/admin/**、/api/admin/**）需要管理员权限</li>
 *   <li>其他接口都需认证</li>
 * </ul>
 * 喵呜～ 这样就又安全又灵活啦！
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserJwtAuthFilter userJwtAuthFilter;
    private final AdminJwtAuthFilter adminJwtAuthFilter;

    public SecurityConfig(
            UserJwtAuthFilter userJwtAuthFilter,
            AdminJwtAuthFilter adminJwtAuthFilter
    ) {
        this.userJwtAuthFilter = userJwtAuthFilter;
        this.adminJwtAuthFilter = adminJwtAuthFilter;
    }

    /**
     * 配置安全过滤链
     * <p>
     * 这是 Spring Security 的核心配置，主要做了这几件事：
     * <ul>
     *   <li>禁用 CSRF（因为用 Cookie + Token 认证，不需要 CSRF 保护）</li>
     *   <li>设置无状态会话（不创建 session，每次请求都通过 JWT 认证）</li>
     *   <li>配置接口访问权限（公开 / 用户 / 管理员 / 全部需认证）</li>
     *   <li>注册自定义的 JWT 认证过滤器</li>
     *   <li>配置跨域支持</li>
     * </ul>
     *
     * @param http HttpSecurity 配置器
     * @return 配置好的 SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF 喵～ 因为咱是 RESTful API，用 JWT 认证，不需要 CSRF 保护
                .csrf(csrf -> csrf.disable())

                // 设置无状态会话，每次请求都得带上 JWT Token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置接口访问权限
                .authorizeHttpRequests(auth -> auth
                        // ===== 公开路径（无需认证） =====
                        .requestMatchers(
                                "/",
                                "/index",
                                "/login",
                                "/register",
                                "/post/**",
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/admin/login",
                                "/api/posts",
                                "/api/posts/**",
                                "/api/files",
                                "/api/files/**",
                                "/static/**",
                                "/assets/**"
                        ).permitAll()

                        // ===== 管理员路径（需要 ADMIN 角色） =====
                        .requestMatchers(
                                "/admin/**",
                                "/api/admin/**",
                                "/api/auth/admin/me",
                                "/api/posts/admin/**",
                                "/api/plugins/**"
                        ).hasRole("ADMIN")

                        // ===== 用户路径（需要登录认证） =====
                        .requestMatchers(
                                "/user/**",
                                "/api/auth/me"
                        ).authenticated()

                        // ===== 其他所有路径都需要认证 =====
                        .anyRequest().authenticated()
                )

                // 注册自定义的 JWT 认证过滤器，放在 UsernamePasswordAuthenticationFilter 之前执行
                .addFilterBefore(userJwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(adminJwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // 配置 CORS 跨域支持
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    /**
     * 配置密码编码器
     * <p>
     * 使用 Argon2id 算法来哈希密码，
     * 这是目前最安全的密码哈希算法喵！
     * 具体实现请看 {@link Argon2PasswordEncoder}
     *
     * @return Argon2id 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder();
    }

    /**
     * 配置 CORS（跨域资源共享）
     * <p>
     * 允许所有来源的跨域请求，方便前端开发调试。
     * 允许所有 HTTP 方法和请求头，
     * 支持携带凭据（Cookie）。
     *
     * @return CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许所有来源的跨域请求喵～
        configuration.addAllowedOriginPattern("*");
        // 允许所有 HTTP 方法
        configuration.addAllowedMethod("*");
        // 允许所有请求头
        configuration.addAllowedHeader("*");
        // 允许携带凭据（Cookie）
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径生效
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}