package xl.xldevblog.main.dto;

import java.time.LocalDateTime;

/**
 * 喵～ 文章列表响应 DTO！
 * 返回给前端的文章信息，
 * 不包含完整的文章内容（content），
 * 只在列表页展示摘要信息喵～
 */
public class PostResponse {

    /** 文章唯一 ID */
    private Long id;

    /** 文章标题 */
    private String title;

    /** URL 别名 */
    private String slug;

    /** 文章内容（Markdown 原文） */
    private String content;

    /** 文章摘要，列表页展示用 */
    private String summary;

    /** 标签，逗号分隔的字符串 */
    private String tags;

    /** 作者名称 */
    private String authorName;

    /** 文章状态（DRAFT / PUBLISHED） */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    // ===== Getter / Setter =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}