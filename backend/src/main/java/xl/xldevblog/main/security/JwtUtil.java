package xl.xldevblog.main.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 喵～ JWT Token 工具类！
 * <p>
 * 负责生成和验证 JWT Token，
 * 支持用户 token 和管理员 token 两种类型。
 * 签名算法用的是 HS256（HMAC-SHA256），
 * 密钥从配置文件中读取，默认也有一个兜底密钥喵！
 * <p>
 * Token 里存了这些信息：
 * <ul>
 *   <li>sub - 用户/管理员 ID</li>
 *   <li>username - 用户名</li>
 *   <li>type - 类型（USER / ADMIN）</li>
 *   <li>iat - 签发时间</li>
 *   <li>exp - 过期时间</li>
 * </ul>
 */
@Component
public class JwtUtil {

    /** 管理员 Token 过期时间（秒），固定 1 小时喵！ */
    private static final long ADMIN_EXPIRATION = 3600L;

    /** HMAC-SHA256 签名密钥，从配置文件中读取 */
    private final SecretKey signingKey;

    /** 用户 Token 过期时间（毫秒），从配置文件中注入 */
    private final long userExpiration;

    /**
     * 构造注入 Secret Key 和用户过期时间
     *
     * @param secret         签名密钥字符串，从 jwt.secret 配置读取
     * @param userExpiration 用户 token 过期时间（毫秒），从 jwt.user-expiration 配置读取
     */
    public JwtUtil(
            @Value("${jwt.secret:XiaoluoDevBlogSecretKeyForJWTTokenGenerationAndValidation2024}")
            String secret,
            @Value("${jwt.user-expiration:604800000}")
            long userExpiration) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.userExpiration = userExpiration;
    }

    /**
     * 生成用户 JWT Token
     * <p>
     * payload 包含用户 ID、用户名、类型标记为 USER，
     * 过期时间使用配置文件中用户 token 的过期时长喵～
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return 生成的 JWT Token 字符串
     */
    public String generateUserToken(Long userId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + userExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("type", "USER")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成管理员 JWT Token
     * <p>
     * payload 包含管理员 ID、用户名、类型标记为 ADMIN，
     * 额外携带 role=ADMIN 字段，
     * 过期时间固定为 1 小时（3600 秒）喵！
     *
     * @param adminId  管理员 ID
     * @param username 管理员用户名
     * @return 生成的 JWT Token 字符串
     */
    public String generateAdminToken(Long adminId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ADMIN_EXPIRATION * 1000);

        return Jwts.builder()
                .subject(adminId.toString())
                .claim("username", username)
                .claim("type", "ADMIN")
                .claim("role", "ADMIN")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证 Token 是否有效
     * <p>
     * 会检查签名是否正确、是否过期。
     * 如果 token 被篡改或已过期，返回 false 喵～
     *
     * @param token JWT Token 字符串
     * @return true = 有效，false = 无效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 从 Token 提取用户 ID
     *
     * @param token JWT Token 字符串
     * @return 用户/管理员 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 提取用户名
     *
     * @param token JWT Token 字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("username", String.class);
    }

    /**
     * 从 Token 提取类型（USER / ADMIN）
     *
     * @param token JWT Token 字符串
     * @return token 类型：USER 或 ADMIN
     */
    public String getTokenType(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("type", String.class);
    }

    /**
     * 获取 Token 的到期时间
     *
     * @param token JWT Token 字符串
     * @return 到期时间
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration();
    }
}