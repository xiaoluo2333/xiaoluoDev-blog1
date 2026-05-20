package xl.xldevblog.main.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 喵～ 博客文章实体！
 * 每篇文章都有自己的 slug（URL 别名），
 * 这样访问的时候就是 /post/hello-world 而不是 /post/123 啦～
 */
@Entity
@Table(name = "posts")
public class Post {

    /** 文章唯一 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 作者（关联到 users 表） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    /** 文章标题 */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * URL 别名，用于生成好看的链接
     * 比如 slug = "my-first-post"，URL 就是 /post/my-first-post
     */
    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    /** 文章正文，Markdown 格式，所以用 LONGTEXT 存大文本 */
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    /** 文章摘要，在列表页展示用的 */
    @Column(length = 500)
    private String summary;

    /** 标签，用逗号分隔，比如 "java,spring,博客" */
    @Column(length = 500)
    private String tags;

    /**
     * 文章状态
     * DRAFT = 草稿，还没发布
     * PUBLISHED = 已发布，大家都能看到
     */
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    /** 发布时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 最后修改时间 */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== Getter / Setter =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}