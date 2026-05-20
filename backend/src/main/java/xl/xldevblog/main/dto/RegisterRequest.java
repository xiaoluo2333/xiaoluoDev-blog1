package xl.xldevblog.main.dto;

/**
 * 喵～ 注册请求 DTO！
 * 新用户注册时需要填写的表单数据，
 * 用户名、密码、邮箱一个都不能少喵！
 */
public class RegisterRequest {

    /** 用户名，唯一不能重复喵～ */
    private String username;

    /** 密码，会经过哈希加密后存储，放心喵 */
    private String password;

    /** 用户邮箱，用于找回密码和通知 */
    private String email;

    // ===== Getter / Setter =====

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}