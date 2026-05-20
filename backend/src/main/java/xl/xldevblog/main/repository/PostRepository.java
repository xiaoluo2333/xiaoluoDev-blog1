package xl.xldevblog.main.repository;

import xl.xldevblog.main.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 喵～ 文章数据访问接口！
 * <p>
 * 负责博客文章的数据库操作，
 * 除了基础 CRUD 之外，
 * 还提供按 slug 查找、按状态分页查询等功能。
 * <p>
 * 所有文章相关的数据库查询都来这里找窝～ (ฅ´ω`ฅ)
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 根据 URL 别名查找文章
     * <p>
     * slug 也是 unique 约束的，
     * 所以敢放心返回 Optional～
     * 这样访问 /post/hello-world 的时候就能找到对应的文章啦！
     *
     * @param slug 文章的 URL 别名
     * @return 包含文章的 Optional，没找到就返回 Optional.empty()
     */
    Optional<Post> findBySlug(String slug);

    /**
     * 按状态筛选文章，并按创建时间倒序排列（最新的在最前面）
     * <p>
     * 比如只查已发布的文章：findByStatusOrderByCreatedAtDesc("PUBLISHED", pageable)
     * 支持分页，前端列表页展示的时候会用到喵～
     *
     * @param status  文章状态（DRAFT / PUBLISHED）
     * @param pageable 分页参数（页码、每页条数、排序方式）
     * @return 分页后的文章列表
     */
    Page<Post> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}