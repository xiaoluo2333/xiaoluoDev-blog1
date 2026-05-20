package xl.xldevblog.main.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 喵～ 文件附件实体！
 * 这个表管理所有上传的文件和创建的文件夹，
 * 支持 Windows 和 Linux 两种路径格式。
 * 用 parent_id 实现文件夹树结构，就像 openlist 那样～
 *
 * ⚠️ 安全提醒：所有文件操作都要防路径穿越！
 *    详见 FileService 中的路径校验逻辑
 */
@Entity
@Table(name = "file_attachments")
public class FileAttachment {

    /** 文件/文件夹唯一 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联的文章（可以为 null，表示独立文件，不绑定文章） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * 文件在磁盘上的实际路径
     * 支持：
     *   Windows: D:\myfiles\docs\readme.txt
     *   Linux:   /opt/data/files/readme.txt
     * ⚠️ 存到数据库之前必须经过路径穿越校验！
     */
    @Column(nullable = false, length = 1000)
    private String filePath;

    /** 文件显示名称，展示给用户看的 */
    @Column(nullable = false, length = 255)
    private String fileName;

    /** 文件大小（字节），文件夹的话就是 0 */
    private Long fileSize;

    /** MIME 类型，比如 text/plain, application/pdf */
    @Column(length = 100)
    private String contentType;

    /** 是否是文件夹（true = 文件夹，false = 文件） */
    @Column(nullable = false)
    private Boolean isFolder = false;

    /**
     * 父文件夹 ID
     * null 表示根目录
     * 非 null 表示这个文件/文件夹在某个文件夹下面
     * 这样就形成了文件夹树结构喵～
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FileAttachment parent;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ===== Getter / Setter =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Boolean getIsFolder() { return isFolder; }
    public void setIsFolder(Boolean isFolder) { this.isFolder = isFolder; }

    public FileAttachment getParent() { return parent; }
    public void setParent(FileAttachment parent) { this.parent = parent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}