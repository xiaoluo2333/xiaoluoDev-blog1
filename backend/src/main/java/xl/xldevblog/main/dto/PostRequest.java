package xl.xldevblog.main.dto;

/**
 * 喵～ 文章创建/更新请求 DTO！
 * 写博客的时候用的表单数据，
 * 创建和更新共用这一个结构喵～
 */
public class PostRequest {

    /** 文章标题，会显示在页面顶部喵 */
    private String title;

    /**
     * URL 别名，用于生成好看的链接
     * 比如 "hello-world"，URL 就是 /post/hello-world
     */
    private String slug;

    /** 文章正文，支持 Markdown 格式喵～ */
    private String content;

    /** 文章摘要，在列表页展示用的简短介绍 */
    private String summary;

    /**
     * 标签，用逗号分隔
     * 比如 "java,spring,喵"
     */
    private String tags;

    /**
     * 文章状态
     * DRAFT     = 草稿，只有自己能看
     * PUBLISHED = 已发布，大家都能看
     */
    private String status;

    // ===== Getter / Setter =====

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
}