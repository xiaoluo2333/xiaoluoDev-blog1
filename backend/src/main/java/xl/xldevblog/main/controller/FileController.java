package xl.xldevblog.main.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xl.xldevblog.main.dto.ApiResponse;
import xl.xldevblog.main.dto.FileResponse;
import xl.xldevblog.main.entity.FileAttachment;
import xl.xldevblog.main.service.FileService;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 喵～ 文件管理控制器！
 * <p>
 * 提供文件和文件夹的 CRUD 接口，
 * 包括上传、下载、创建目录、删除等操作。
 * <p>
 * 所有接口路径都以 /api/files 开头，
 * 跟 SecurityConfig 里的权限配置保持一致喵～
 * <p>
 * ⚠️ 安全提醒：
 * <ul>
 *   <li>文件上传、创建文件夹、删除需要 ADMIN 权限</li>
 *   <li>文件列表浏览和下载对游客开放</li>
 *   <li>路径穿越防护由 FileService 的 sanitizePath 保障</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 喵～ 创建文件夹的请求体结构！
     * <p>
     * 前端 POST /api/files/folder 的时候，
     * 需要传这个 JSON 格式的数据喵～
     * <pre>
     * {
     *   "name": "我的文件夹",
     *   "parentId": null
     * }
     * </pre>
     */
    public static class CreateFolderRequest {
        private String name;
        private Long parentId;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
    }

    // =======================================================================
    //                         GET — 公开接口
    // =======================================================================

    /**
     * 喵～ 获取根目录文件列表！
     * <p>
     * GET /api/files
     * <p>
     * 返回根目录下的所有文件和文件夹，
     * 用于初始化文件管理器界面喵～
     *
     * @return 根目录下所有文件和文件夹的列表
     */
    @GetMapping
    public ApiResponse getRootFiles() {
        log.debug("喵～ 获取根目录文件列表");
        List<FileResponse> files = fileService.getRootFiles();
        return ApiResponse.ok(files);
    }

    /**
     * 喵～ 获取文章关联的附件列表！
     * <p>
     * GET /api/files/bypost/{postId}
     * <p>
     * 在编辑文章的时候，需要展示这篇文章上传了哪些文件，
     * 比如图片、文档等附件。这个接口就是用来干这个的喵～
     *
     * @param postId 文章的 ID
     * @return 该文章关联的所有文件附件列表
     */
    @GetMapping("/bypost/{postId}")
    public ApiResponse getFilesByPost(@PathVariable Long postId) {
        log.debug("喵～ 获取文章的附件列表: postId={}", postId);
        List<FileAttachment> files = fileService.getFilesByPost(postId);
        return ApiResponse.ok(files);
    }

    /**
     * 喵～ 获取指定文件夹的内容！
     * <p>
     * GET /api/files/{parentId}
     * <p>
     * 返回某个文件夹下的所有直接子文件/子文件夹，
     * 双击文件夹后调这个接口就能展开目录树啦～
     * <p>
     * 注意：{parentId} 不能是 "download"，
     * 因为 /api/files/download/{id} 是下载接口，
     * Spring 会自动区分开喵！
     *
     * @param parentId 父文件夹的 ID
     * @return 该文件夹下的所有文件和文件夹列表
     */
    @GetMapping("/{parentId}")
    public ApiResponse getFilesByParent(@PathVariable Long parentId) {
        log.debug("喵～ 获取文件夹内容: parentId={}", parentId);
        List<FileResponse> files = fileService.getFilesByParent(parentId);
        return ApiResponse.ok(files);
    }

    /**
     * 喵～ 下载文件！
     * <p>
     * GET /api/files/download/{id}
     * <p>
     * 根据文件 ID 下载对应的文件，
     * 会经过 FileService 的 sanitizePath 路径穿越校验，
     * 确保安全后才返回文件内容喵～
     * <p>
     * 设置了 Content-Disposition 头，
     * 让浏览器直接弹出下载对话框而不是在浏览器里打开。
     *
     * @param id 要下载的文件 ID
     * @return 文件内容，以附件形式下载
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        // step 1: 通过 FileService 获取经过路径穿越校验的文件实体
        FileAttachment file = fileService.downloadFile(id);

        try {
            // step 2: 基于已校验的路径构建文件资源
            Path filePath = Paths.get(file.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("喵呜… 文件在磁盘上不存在或不可读: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // step 3: 确定 Content-Type（根据数据库记录或自动检测）
            String contentType = file.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = Files.probeContentType(filePath);
            }
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            // step 4: 构建下载响应，让浏览器弹出下载框
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(file.getFileSize() != null ? file.getFileSize() : resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);

        } catch (Exception e) {
            log.error("喵呜… 文件下载失败: id={}, {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // =======================================================================
    //                        POST — 需要 ADMIN 权限
    // =======================================================================

    /**
     * 喵～ 上传文件！
     * <p>
     * POST /api/files/upload
     * <p>
     * 需要 ADMIN 权限（@PreAuthorize 守卫）。
     * 支持上传到指定文件夹和关联到指定文章。
     * <p>
     * 上传的文件会用 UUID 重命名后保存到 upload-dir 目录，
     * 原始文件名会保留在数据库的 fileName 字段中喵～
     *
     * @param file     上传的文件（MultipartFile）
     * @param parentId 父文件夹 ID（可选，null 表示根目录）
     * @param postId   关联的文章 ID（可选，null 表示不关联文章）
     * @return 创建好的文件附件信息
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse uploadFile(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Long postId) {
        log.info("喵～ 上传文件: name={}, size={}, parentId={}, postId={}",
            file.getOriginalFilename(), file.getSize(), parentId, postId);

        if (file.isEmpty()) {
            return ApiResponse.error("喵呜… 上传的文件是空的喵～");
        }

        FileAttachment saved = fileService.uploadFile(file, parentId, postId);
        return ApiResponse.ok(saved);
    }

    /**
     * 喵～ 创建文件夹！
     * <p>
     * POST /api/files/folder
     * <p>
     * 需要 ADMIN 权限（@PreAuthorize 守卫）。
     * 在文件系统上创建目录并在数据库中创建对应的记录，
     * 支持在根目录或指定父文件夹下创建喵～
     *
     * @param request 包含文件夹名称和父文件夹 ID 的请求体
     * @return 创建好的文件夹信息
     */
    @PostMapping("/folder")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse createFolder(@RequestBody CreateFolderRequest request) {
        log.info("喵～ 创建文件夹: name={}, parentId={}", request.getName(), request.getParentId());

        if (request.getName() == null || request.getName().isBlank()) {
            return ApiResponse.error("喵呜… 文件夹名称不能为空喵～");
        }

        FileAttachment folder = fileService.createFolder(request.getName(), request.getParentId());
        return ApiResponse.ok(folder);
    }

    // =======================================================================
    //                       DELETE — 需要 ADMIN 权限
    // =======================================================================

    /**
     * 喵～ 删除文件或文件夹！
     * <p>
     * DELETE /api/files/{id}
     * <p>
     * 需要 ADMIN 权限（@PreAuthorize 守卫）。
     * 如果是文件夹会递归删除所有子文件和子文件夹，
     * 数据库记录和磁盘文件会一起清理喵～
     *
     * @param id 要删除的文件或文件夹 ID
     * @return 操作结果消息
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse deleteFile(@PathVariable Long id) {
        log.info("喵～ 删除文件: id={}", id);
        fileService.deleteFile(id);
        return ApiResponse.ok("已删除喵～");
    }
}