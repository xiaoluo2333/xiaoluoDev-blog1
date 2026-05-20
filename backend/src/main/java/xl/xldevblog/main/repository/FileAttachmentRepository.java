package xl.xldevblog.main.repository;

import xl.xldevblog.main.entity.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 喵～ 文件附件数据访问接口！
 * <p>
 * 管理所有上传文件和文件夹的数据库操作。
 * 通过 parentId 可以实现文件夹树的遍历，
 * 通过 postId 可以找到某篇文章关联的所有附件。
 * <p>
 * 文件操作要注意安全喵！路径穿越防护不能忘！ ⚠️(｀・ω・´)
 */
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    /**
     * 根据父文件夹 ID 查找所有直接子文件/子文件夹
     * <p>
     * 如果 parentId 为 null 就查根目录下的文件，
     * 不为 null 就查某个文件夹里面有什么，
     * 这样就实现文件夹树结构啦～
     *
     * @param parentId 父文件夹的 ID（可为 null）
     * @return 该文件夹下的所有直接子文件/子文件夹列表
     */
    List<FileAttachment> findByParentId(Long parentId);

    /**
     * 根据文章 ID 查找该文章关联的所有附件
     * <p>
     * 比如在编辑文章的时候，
     * 需要显示这篇文章上传了哪些文件，
     * 就用这个方法查喵～
     *
     * @param postId 文章的 ID
     * @return 该文章关联的文件附件列表
     */
    List<FileAttachment> findByPostId(Long postId);
}