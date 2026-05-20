package xl.xldevblog.main.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 喵～ 用户实体！
 * 这张表同时存普通用户和管理员，
 * 通过 is_admin 字段来区分。
 * 管理员在整个系统里是唯一的哟！
 */
@Entity
@Table(name = "users")
public class User {

    /** 用户唯一 ID，自增主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名，唯一不能重复喵 */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Argon2id 哈希后的密码，绝对不能存明文！ */
    @Column(nullable = false, length = 255)
    private String passwordHash;

    /** 用户邮箱，注册时填的 */
    @Column(unique = true, length = 100)
    private String email;

    /** 显示名称，不填的话默认和用户名一样 */
    @Column(length = 100)
    private String displayName;

    /**
     * 是否是管理员喵～
     * true = 管理员（系统唯一，初始化时创建的）
     * false = 普通用户（可以注册多个）
     */
    @Column(nullable = false)
    private Boolean isAdmin = false;

    /** 账号创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 账号信息最后更新时间 */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ===== 生命周期回调 =====
    // 在插入前自动设置创建时间和更新时间
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // 在更新前自动更新时间戳
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== Getter / Setter =====
    // 用了最传统的方式，因为不用 Lombok 也能让其他人看得懂喵

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}