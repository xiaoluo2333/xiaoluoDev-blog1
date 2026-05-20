package xl.xldevblog.main.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xl.xldevblog.main.dto.FileResponse;
import xl.xldevblog.main.entity.FileAttachment;
import xl.xldevblog.main.entity.Post;
import xl.xldevblog.main.repository.FileAttachmentRepository;
import xl.xldevblog.main.repository.PostRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 喵～ 文件管理服务！
 * <p>
 * 负责所有文件和文件夹的增删改查操作，
 * 包括上传、下载、创建文件夹、删除文件等。
 * <p>
 * ⚠️ 安全重中之重：所有涉及文件系统路径的操作，
 * 都必须经过 sanitizePath() 进行路径穿越校验！
 * 绝对不能让坏猫通过 ../ 逃出 upload-dir 喵！(｀・ω・´)
 */
@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    /** 上传文件存放目录，从 application.yml 的 file.upload-dir 读取 */
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /** 下载文件存放目录，从 application.yml 的 file.download-dir 读取 */
    @Value("${file.download-dir:./downloads}")
    private String downloadDir;

    /** 最大文件大小（50MB），超过这个大小的上传请求会被拒绝喵 */
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024L;

    private final FileAttachmentRepository fileAttachmentRepository;
    private final PostRepository postRepository;

    public FileService(FileAttachmentRepository fileAttachmentRepository, PostRepository postRepository) {
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.postRepository = postRepository;
    }

    // =======================================================================
    //                           路径穿越防护（核心安全逻辑）
    // =======================================================================

    /**
     * 喵～ 路径穿越防护方法！
     * <p>
     * 这是整个文件管理系统的安全基石，
     * 所有涉及文件系统路径的操作都必须调用这个方法进行校验！
     * <p>
     * 它会解析输入路径中的 .. 和符号链接，
     * 然后检查解析后的路径是否在基目录下，
     * 如果试图逃出基目录就会抛出 SecurityException 喵！(╯°□°)╯
     *
     * @param inputPath 用户传入的路径（可能包含 ../ 等恶意内容）
     * @param baseDir   合法的基目录，解析后的路径必须在这个目录下
     * @return 经过校验的安全规范路径
     * @throws SecurityException 如果检测到路径穿越行为
     */
    private String sanitizePath(String inputPath, String baseDir) {
        // 1. 用 File 对象解析路径，获取规范化绝对路径
        File baseDirFile = new File(baseDir).getAbsoluteFile();
        File resolvedFile = new File(baseDirFile, inputPath).getAbsoluteFile();

        // 2. 调用 getCanonicalPath() 解析 .. 和符号链接
        String canonicalPath;
        String baseCanonicalPath;
        try {
            canonicalPath = resolvedFile.getCanonicalPath();
            baseCanonicalPath = baseDirFile.getCanonicalPath();
        } catch (IOException e) {
            throw new SecurityException("坏猫！路径解析失败了喵！(╯°□°)╯", e);
        }

        // 3. 检查解析后的路径是否在基目录下
        if (!canonicalPath.startsWith(baseCanonicalPath + File.separator)
            && !canonicalPath.equals(baseCanonicalPath)) {
            throw new SecurityException("坏猫！路径穿越是不允许的喵！(╯°□°)╯");
        }

        return canonicalPath;
    }

    // =======================================================================
    //                          实体转 DTO 辅助方法
    // =======================================================================

    /**
     * 喵～ 把 FileAttachment 实体转换成 FileResponse DTO！
     * <p>
     * 这样前端展示文件列表的时候，
     * 就不会暴露内部的 filePath 等敏感信息啦～
     *
     * @param entity 数据库中的文件附件实体
     * @return 返回给前端的文件信息 DTO
     */
    private FileResponse toFileResponse(FileAttachment entity) {
        FileResponse response = new FileResponse();
        response.setId(entity.getId());
        response.setFileName(entity.getFileName());
        response.setFileSize(entity.getFileSize());
        response.setContentType(entity.getContentType());
        response.setIsFolder(entity.getIsFolder());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    // =======================================================================
    //                          公开业务方法
    // =======================================================================

    /**
     * 喵～ 获取根目录文件列表！
     * <p>
     * 查询所有 parent 为 null 的文件和文件夹，
     * 也就是最顶层的内容～
     *
     * @return 根目录下的文件和文件夹列表
     */
    public List<FileResponse> getRootFiles() {
        return fileAttachmentRepository.findAll().stream()
            .filter(f -> f.getParent() == null)
            .map(this::toFileResponse)
            .collect(Collectors.toList());
    }

    /**
     * 喵～ 获取指定文件夹的内容！
     * <p>
     * 根据 parentId 查找某个文件夹下的所有直接子文件/子文件夹，
     * 这样就实现了文件夹树的浏览功能啦～
     *
     * @param parentId 父文件夹的 ID
     * @return 该文件夹下的所有直接子文件/子文件夹列表
     * @throws RuntimeException 如果父文件夹不存在
     */
    public List<FileResponse> getFilesByParent(Long parentId) {
        if (parentId == null) {
            return getRootFiles();
        }

        // 先确认父文件夹存在
        fileAttachmentRepository.findById(parentId)
            .filter(FileAttachment::getIsFolder)
            .orElseThrow(() -> new RuntimeException("喵呜… 父文件夹没找到或者不是文件夹喵～"));

        return fileAttachmentRepository.findByParentId(parentId).stream()
            .map(this::toFileResponse)
            .collect(Collectors.toList());
    }

    /**
     * 喵～ 创建文件夹！
     * <p>
     * 在数据库中创建 FileAttachment 记录（isFolder=true），
     * 同时在文件系统上创建对应的目录。
     * 目录名使用 UUID 生成，防止重名冲突喵～
     *
     * @param name     文件夹显示名称
     * @param parentId 父文件夹 ID（null 表示在根目录下创建）
     * @return 创建好的文件夹实体
     * @throws RuntimeException 如果父文件夹不存在或创建目录失败
     */
    public FileAttachment createFolder(String name, Long parentId) {
        // 在文件系统上创建目录（用 UUID 防止重名）
        String folderUuid = UUID.randomUUID().toString();
        File dir = new File(new File(uploadDir), folderUuid);
        if (!dir.mkdirs()) {
            throw new RuntimeException("喵呜… 创建文件夹目录失败了喵～");
        }

        // 创建数据库记录
        FileAttachment attachment = new FileAttachment();
        attachment.setFileName(name);
        attachment.setFilePath(dir.getAbsolutePath());
        attachment.setIsFolder(true);
        attachment.setFileSize(0L);

        // 设置父文件夹
        if (parentId != null) {
            FileAttachment parent = fileAttachmentRepository.findById(parentId)
                .filter(FileAttachment::getIsFolder)
                .orElseThrow(() -> new RuntimeException("喵呜… 父文件夹没找到或者不是文件夹喵～"));
            attachment.setParent(parent);
        }

        FileAttachment saved = fileAttachmentRepository.save(attachment);
        log.info("喵～ 文件夹 [{}] 创建成功！路径: {}", name, dir.getAbsolutePath());
        return saved;
    }

    /**
     * 喵～ 上传文件！
     * <p>
     * 保存上传的文件到 upload-dir 目录，
     * 使用 UUID 重命名文件防止重名和路径穿越攻击，
     * 同时在数据库中创建对应的 FileAttachment 记录喵～
     *
     * @param file     上传的 MultipartFile
     * @param parentId 父文件夹 ID（null 表示放在根目录下）
     * @param postId   关联的文章 ID（null 表示不绑定到任何文章）
     * @return 创建好的文件附件实体
     * @throws RuntimeException 如果文件太大、父文件夹不存在或文章不存在
     */
    public FileAttachment uploadFile(MultipartFile file, Long parentId, Long postId) {
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException(
                String.format("喵呜… 文件太大了喵！最大支持 %dMB，你这个有 %.2fMB 了(｡•́︿•̀｡)",
                    MAX_FILE_SIZE / (1024 * 1024),
                    file.getSize() / (1024.0 * 1024.0)));
        }

        // 获取原始文件名和扩展名
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed_file";
        }

        String ext = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = originalName.substring(dotIndex);
        }

        // 用 UUID 重命名文件，防止重名和路径穿越
        String uuidName = UUID.randomUUID().toString() + ext;
        File targetFile = new File(new File(uploadDir), uuidName);

        try {
            // 确保 upload-dir 目录存在
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }

            // 保存文件到磁盘
            file.transferTo(targetFile);

            // 创建数据库记录
            FileAttachment attachment = new FileAttachment();
            attachment.setFileName(originalName);
            attachment.setFilePath(targetFile.getAbsolutePath());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setIsFolder(false);

            // 设置父文件夹
            if (parentId != null) {
                FileAttachment parent = fileAttachmentRepository.findById(parentId)
                    .filter(FileAttachment::getIsFolder)
                    .orElseThrow(() -> new RuntimeException("喵呜… 父文件夹没找到或者不是文件夹喵～"));
                attachment.setParent(parent);
            }

            // 设置关联的文章
            if (postId != null) {
                Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("喵呜… 关联的文章没找到喵～"));
                attachment.setPost(post);
            }

            FileAttachment saved = fileAttachmentRepository.save(attachment);
            log.info("喵～ 文件 [{}] 上传成功！大小: {} 字节, 路径: {}",
                originalName, file.getSize(), targetFile.getAbsolutePath());
            return saved;

        } catch (IOException e) {
            // 如果保存失败，清理已创建的文件
            if (targetFile.exists()) {
                targetFile.delete();
            }
            log.error("喵呜… 文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("喵呜… 文件上传失败了喵～ " + e.getMessage());
        }
    }

    /**
     * 喵～ 下载文件！
     * <p>
     * 根据文件 ID 查找 FileAttachment 记录，
     * 获取文件路径后使用 sanitizePath 进行路径穿越校验，
     * 确保文件在合法的 upload-dir 目录下，
     * 防止坏猫通过篡改数据库路径来读取敏感文件！(｀・ω・´)
     *
     * @param id 文件 ID
     * @return 经过路径穿越校验的文件附件实体
     * @throws RuntimeException 如果文件没找到或路径校验不通过
     */
    public FileAttachment downloadFile(Long id) {
        FileAttachment file = fileAttachmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("喵呜… 文件没找到喵～"));

        // ⚠️ 核心安全校验：路径穿越防护！
        String safePath = sanitizePath(file.getFilePath(), uploadDir);
        log.debug("喵～ 文件下载路径校验通过: {}", safePath);

        return file;
    }

    /**
     * 喵～ 读取文件为 Resource！
     * <p>
     * 在 downloadFile 的基础上，读取文件的字节内容返回，
     * 用于控制层直接构建下载响应。
     *
     * @param id 文件 ID
     * @return 文件的字节内容
     * @throws RuntimeException 如果文件读取失败
     */
    public byte[] loadFileBytes(Long id) {
        FileAttachment file = downloadFile(id);

        // 先通过 downloadFile 完成路径校验，再用校验后的路径读取文件
        String safePath = sanitizePath(file.getFilePath(), uploadDir);
        try {
            return Files.readAllBytes(Paths.get(safePath));
        } catch (IOException e) {
            log.error("喵呜… 文件读取失败: {}", e.getMessage(), e);
            throw new RuntimeException("喵呜… 文件读取失败了喵～ " + e.getMessage());
        }
    }

    /**
     * 喵～ 删除文件或文件夹！
     * <p>
     * 如果是文件夹，会递归删除里面的所有子文件和子文件夹。
     * 删除顺序：先删数据库记录 → 再删磁盘文件，
     * 如果磁盘文件删失败了不会影响数据库的删除喵～
     *
     * @param id 要删除的文件或文件夹 ID
     * @throws RuntimeException 如果文件没找到
     */
    public void deleteFile(Long id) {
        FileAttachment file = fileAttachmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("喵呜… 要删除的文件没找到喵～"));

        // 如果是文件夹，递归删除子文件
        if (file.getIsFolder()) {
            List<FileAttachment> children = fileAttachmentRepository.findByParentId(id);
            for (FileAttachment child : children) {
                deleteFile(child.getId());
            }
        }

        // ⚠️ 安全校验：删除前也要校验路径，防止恶意操作
        String safePath = sanitizePath(file.getFilePath(), uploadDir);

        // 删除数据库记录
        fileAttachmentRepository.delete(file);

        // 删除磁盘上的实际文件/目录
        try {
            File diskFile = new File(safePath);
            if (diskFile.exists()) {
                if (diskFile.delete()) {
                    log.debug("喵～ 磁盘文件已删除: {}", safePath);
                } else {
                    log.warn("喵… 磁盘文件删除失败（可能是目录非空）: {}", safePath);
                }
            }
        } catch (Exception e) {
            // 磁盘删除失败不影响数据库删除，只记录日志
            log.warn("喵… 删除磁盘文件时出现异常: {}", e.getMessage());
        }

        log.info("喵～ 文件/文件夹 [{}] 已删除！", file.getFileName());
    }

    /**
     * 喵～ 获取文章关联的附件列表！
     * <p>
     * 在编辑文章的时候，
     * 需要显示这篇文章上传了哪些文件，
     * 比如图片、文档等附件喵～
     *
     * @param postId 文章的 ID
     * @return 该文章关联的所有文件附件
     */
    public List<FileAttachment> getFilesByPost(Long postId) {
        return fileAttachmentRepository.findByPostId(postId);
    }
}