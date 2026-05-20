package xl.xldevblog.main.dto;

/**
 * 喵～ 登录响应 DTO！
 * 登录成功后返回给前端的信息，
 * 包含 token、用户名和过期时间喵～
 */
public class LoginResponse {

    /** JWT token，后续请求都要带着这个喵 */
    private String token;

    /** 登录成功的用户名 */
    private String username;

    /** token 过期时间（秒），前端可以根据这个来提前刷新 token */
    private long expiresIn;

    // ===== Getter / Setter =====

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}