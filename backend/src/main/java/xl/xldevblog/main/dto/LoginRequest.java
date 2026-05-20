package xl.xldevblog.main.dto;

/**
 * 喵～ 登录请求 DTO！
 * 用户登录时填写的表单数据，
 * 包含用户名密码和"记住我"选项喵～
 */
public class LoginRequest {

    /** 用户名，不能为空喵 */
    private String username;

    /** 密码，不能为空喵 */
    private String password;

    /**
     * 是否勾选了"记住我"～
     * true  = 记住登录状态，token 有效期更长
     * false = 只在当前会话有效
     */
    private boolean rememberMe;

    // ===== Getter / Setter =====

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }
}