package xl.xldevblog.main.dto;

import java.time.LocalDateTime;

/**
 * 喵～ 文件列表响应 DTO！
 * 返回给前端的文件/文件夹信息，
 * 用于展示文件列表和目录树结构喵～
 */
public class FileResponse {

    /** 文件/文件夹唯一 ID */
    private Long id;

    /** 文件显示名称 */
    private String fileName;

    /** 文件大小（字节），文件夹则为 0 */
    private Long fileSize;

    /** MIME 类型，比如 text/plain, image/png */
    private String contentType;

    /** 是否是文件夹（true = 文件夹，false = 文件） */
    private Boolean isFolder;

    /** 创建时间 */
    private LocalDateTime createdAt;

    // ===== Getter / Setter =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Boolean getIsFolder() { return isFolder; }
    public void setIsFolder(Boolean isFolder) { this.isFolder = isFolder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}