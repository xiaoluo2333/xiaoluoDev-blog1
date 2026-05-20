package xl.xldevblog.main.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xl.xldevblog.main.dto.LoginResponse;
import xl.xldevblog.main.dto.RegisterRequest;
import xl.xldevblog.main.entity.User;
import xl.xldevblog.main.repository.UserRepository;
import xl.xldevblog.main.security.JwtUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 喵～ 认证服务类！
 * <p>
 * 处理用户注册、登录、管理员登录等认证相关的业务逻辑。
 * 密码全部使用 Spring Security 的 PasswordEncoder（Argon2id）哈希存储，
 * 绝对不会泄露明文密码的喵！(๑•̀ㅂ•́)و✧
 * <p>
 * Token 的生成使用 JwtUtil，用户 token 和管理员 token 是分开的，
 * 这样前端可以分别管理用户会话和管理员会话～
 */
@Service
public class AuthService {

    /** 日志记录器，用来输出认证相关的日志信息 */
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** 用户数据仓库，用来操作数据库中的用户表 */
    private final UserRepository userRepository;

    /** JWT Token 工具类，用来生成和解析 token */
    private final JwtUtil jwtUtil;

    /**
     * 密码编码器，使用 Argon2id 算法对密码进行哈希
     * 注入的是 Spring Security 的 PasswordEncoder 接口，
     * 实际实现是 Argon2PasswordEncoder 喵～
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造注入三个依赖
     *
     * @param userRepository   用户数据仓库
     * @param jwtUtil          JWT Token 工具类
     * @param passwordEncoder  密码编码器（Argon2id）
     */
    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 喵～ 用户注册！
     * <p>
     * 注册流程：
     * <ol>
     *   <li>检查用户名是否已被注册</li>
     *   <li>检查邮箱是否已被注册</li>
     *   <li>使用 PasswordEncoder 对密码进行哈希</li>
     *   <li>创建 User 实体，isAdmin 默认 false（普通用户）</li>
     *   <li>保存到数据库并返回注册成功的用户</li>
     * </ol>
     *
     * @param req 注册请求，包含用户名、密码、邮箱
     * @return 注册成功的 User 实体
     * @throws IllegalArgumentException 如果用户名或邮箱已存在，抛出此异常
     */
    public User register(RegisterRequest req) {
        String username = req.getUsername();
        String email = req.getEmail();

        if (userRepository.existsByUsername(username)) {
            log.warn("注册失败：用户名 {} 已被注册了喵～", username);
            throw new IllegalArgumentException("用户名已被注册了喵～");
        }

        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            log.warn("注册失败：邮箱 {} 已被注册了喵～", email);
            throw new IllegalArgumentException("邮箱已被注册了喵～");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setEmail(email);
        user.setDisplayName(username);
        user.setIsAdmin(false);

        User savedUser = userRepository.save(user);
        log.info("新用户注册成功喵～ ID: {}, 用户名: {}", savedUser.getId(), savedUser.getUsername());
        return savedUser;
    }

    /**
     * 喵～ 用户登录！
     * <p>
     * 登录流程：
     * <ol>
     *   <li>根据用户名查找用户</li>
     *   <li>用 PasswordEncoder.matches() 验证密码</li>
     *   <li>调用 JwtUtil.generateUserToken() 生成用户 token</li>
     *   <li>计算过期时间（rememberMe 为 true 时 7 天，否则 1 天）</li>
     *   <li>返回 LoginResponse（包含 token、用户名、过期时间）</li>
     * </ol>
     *
     * @param username   用户名
     * @param password   明文密码
     * @param rememberMe 是否记住我（true = 7天过期，false = 1天过期）
     * @return 登录响应，包含 token、用户名、过期时间
     * @throws IllegalArgumentException 如果用户不存在或密码错误，抛出此异常
     */
    public LoginResponse login(String username, String password, boolean rememberMe) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("登录失败：用户 {} 不存在的说～", username);
                    return new IllegalArgumentException("用户名或密码错误喵～");
                });

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("登录失败：用户 {} 的密码验证没通过喵～", username);
            throw new IllegalArgumentException("用户名或密码错误喵～");
        }

        String token = jwtUtil.generateUserToken(user.getId(), user.getUsername());

        long expiresIn = rememberMe ? 604800L : 86400L;

        log.info("用户 {} 登录成功喵～ rememberMe: {}", username, rememberMe);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setExpiresIn(expiresIn);
        return response;
    }

    /**
     * 喵～ 管理员登录！
     * <p>
     * 管理员登录流程：
     * <ol>
     *   <li>根据用户名查找用户</li>
     *   <li>检查该用户是否为管理员（isAdmin = true）</li>
     *   <li>验证密码</li>
     *   <li>调用 JwtUtil.generateAdminToken() 生成管理员 token（1 小时硬编码）</li>
     *   <li>返回 LoginResponse</li>
     * </ol>
     *
     * @param username 管理员用户名
     * @param password 管理员密码
     * @return 登录响应，包含 token、用户名、过期时间（1 小时）
     * @throws IllegalArgumentException 如果用户不存在、不是管理员或密码错误，抛出此异常
     */
    public LoginResponse adminLogin(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("管理员登录失败：用户 {} 不存在的说～", username);
                    return new IllegalArgumentException("管理员账号或密码错误喵～");
                });

        if (!Boolean.TRUE.equals(user.getIsAdmin())) {
            log.warn("管理员登录失败：用户 {} 不是管理员的喵！", username);
            throw new IllegalArgumentException("管理员账号或密码错误喵～");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("管理员登录失败：用户 {} 的密码验证没通过喵～", username);
            throw new IllegalArgumentException("管理员账号或密码错误喵～");
        }

        String token = jwtUtil.generateAdminToken(user.getId(), user.getUsername());

        log.info("管理员 {} 登录成功喵～", username);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setExpiresIn(3600L);
        return response;
    }

    /**
     * 喵～ 获取当前登录用户的信息！
     * <p>
     * 根据用户 ID 查找用户信息，
     * 返回安全的用户信息（不包含密码哈希），
     * 前端可以用这个接口来判断用户是否已登录喵～
     *
     * @param userId 用户 ID
     * @return 安全的用户信息 Map（包含 id、username、email、displayName、createdAt）
     * @throws IllegalArgumentException 如果用户不存在，抛出此异常
     */
    public Map<String, Object> getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("获取用户信息失败：用户 ID {} 不存在的说～", userId);
                    return new IllegalArgumentException("用户不存在喵～");
                });

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("displayName", user.getDisplayName());
        userInfo.put("isAdmin", user.getIsAdmin());
        userInfo.put("createdAt", user.getCreatedAt());
        return userInfo;
    }

    /**
     * 获取当前登录管理员的信息
     * <p>
     * 根据管理员 ID 查找用户信息，
     * 并确认该用户确实是管理员，
     * 返回安全的管理员信息（不包含密码哈希）喵～
     *
     * @param adminId 管理员 ID
     * @return 安全的管理员信息 Map（包含 id、username、email、displayName、createdAt）
     * @throws IllegalArgumentException 如果管理员不存在或不是管理员，抛出此异常
     */
    public Map<String, Object> getCurrentAdmin(Long adminId) {
        User user = userRepository.findById(adminId)
                .orElseThrow(() -> {
                    log.warn("获取管理员信息失败：管理员 ID {} 不存在的说～", adminId);
                    return new IllegalArgumentException("管理员不存在喵～");
                });

        if (!Boolean.TRUE.equals(user.getIsAdmin())) {
            log.warn("获取管理员信息失败：用户 ID {} 不是管理员的喵！", adminId);
            throw new IllegalArgumentException("管理员不存在喵～");
        }

        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("id", user.getId());
        adminInfo.put("username", user.getUsername());
        adminInfo.put("email", user.getEmail());
        adminInfo.put("displayName", user.getDisplayName());
        adminInfo.put("isAdmin", user.getIsAdmin());
        adminInfo.put("createdAt", user.getCreatedAt());
        return adminInfo;
    }
}