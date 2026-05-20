package xl.xldevblog.main.service;

import xl.xldevblog.main.dto.PostRequest;
import xl.xldevblog.main.dto.PostResponse;
import xl.xldevblog.main.entity.Post;
import xl.xldevblog.main.entity.User;
import xl.xldevblog.main.repository.PostRepository;
import xl.xldevblog.main.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 喵～ 博客文章服务！
 * <p>
 * 负责文章相关的所有业务逻辑，
 * 包括创建、更新、删除、查询等操作。
 * 文章的 slug 自动生成、状态管理都在这里处理喵～
 * <p>
 * 所有 public 方法都经过事务管理，
 * 确保数据一致性喵！(ฅ´ω`ฅ)
 */
@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * 喵～ 获取已发布的文章列表！
     * <p>
     * 只返回 status = PUBLISHED 的文章，
     * 按创建时间倒序排列，最新的在最前面。
     * 分页参数由前端控制喵～
     *
     * @param page 页码，从 0 开始
     * @param size 每页条数
     * @return 分页后的已发布文章列表
     */
    public Page<PostResponse> getPosts(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED", pageable);
        return postPage.map(this::toPostResponse);
    }

    /**
     * 喵～ 根据 slug 获取单篇文章！
     * <p>
     * 通过文章的 URL 别名查找，
     * 不管文章是什么状态都能查到（管理员用）。
     * 没找到的话返回 null，由调用方处理喵～
     *
     * @param slug 文章的 URL 别名
     * @return 文章响应 DTO，没找到返回 null
     */
    public PostResponse getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug).orElse(null);
        if (post == null) {
            return null;
        }
        return toPostResponse(post);
    }

    /**
     * 喵～ 管理员获取所有文章（含草稿）！
     * <p>
     * 不限制文章状态，草稿和已发布的都会返回。
     * 也是按创建时间倒序排列喵～
     *
     * @param page 页码，从 0 开始
     * @param size 每页条数
     * @return 分页后的所有文章列表
     */
    public Page<PostResponse> getAllPosts(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.findAll(pageable);
        return postPage.map(this::toPostResponse);
    }

    /**
     * 喵～ 创建新文章！
     * <p>
     * 如果请求中没有传 slug，会自动从 title 生成：
     * 小写 + 连字符风格，比如 "Hello World" → "hello-world"。
     * 还会检查 slug 是否已存在，避免冲突喵～
     *
     * @param req      文章创建请求 DTO
     * @param authorId 作者的用户 ID
     * @return 创建成功的文章响应 DTO
     * @throws IllegalArgumentException 如果 slug 已存在
     */
    public PostResponse createPost(PostRequest req, Long authorId) {
        String slug = req.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = generateSlug(req.getTitle());
        }

        if (postRepository.findBySlug(slug).isPresent()) {
            throw new IllegalArgumentException("slug 已被占用啦，换一个吧喵～：" + slug);
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("作者不存在喵～ ID: " + authorId));

        Post post = new Post();
        post.setAuthor(author);
        post.setTitle(req.getTitle());
        post.setSlug(slug);
        post.setContent(req.getContent());
        post.setSummary(req.getSummary());
        post.setTags(req.getTags());
        post.setStatus(req.getStatus() != null ? req.getStatus() : "DRAFT");

        Post savedPost = postRepository.save(post);
        return toPostResponse(savedPost);
    }

    /**
     * 喵～ 更新文章！
     * <p>
     * 根据 ID 查找文章，更新所有可编辑字段。
     * 注意：不更新 slug，因为 slug 一旦生成就不建议改了，
     * 否则旧链接会失效喵～
     *
     * @param id  文章 ID
     * @param req 文章更新请求 DTO
     * @return 更新后的文章响应 DTO
     * @throws IllegalArgumentException 如果文章不存在
     */
    public PostResponse updatePost(Long id, PostRequest req) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在喵～ ID: " + id));

        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setSummary(req.getSummary());
        post.setTags(req.getTags());
        if (req.getStatus() != null) {
            post.setStatus(req.getStatus());
        }

        Post updatedPost = postRepository.save(post);
        return toPostResponse(updatedPost);
    }

    /**
     * 喵～ 删除文章！
     * <p>
     * 根据 ID 直接删除文章记录。
     * 删除后相关链接将无法访问，请谨慎操作喵～
     *
     * @param id 要删除的文章 ID
     * @throws IllegalArgumentException 如果文章不存在
     */
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new IllegalArgumentException("文章不存在喵～ ID: " + id);
        }
        postRepository.deleteById(id);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 喵～ 将 Post 实体转换为 PostResponse DTO！
     * <p>
     * 提取 Post 中的字段，组装成前端需要的响应格式。
     * authorName 从关联的 User 实体的 displayName 获取。
     *
     * @param post 文章实体
     * @return 文章响应 DTO
     */
    private PostResponse toPostResponse(Post post) {
        PostResponse resp = new PostResponse();
        resp.setId(post.getId());
        resp.setTitle(post.getTitle());
        resp.setSlug(post.getSlug());
        resp.setSummary(post.getSummary());
        resp.setTags(post.getTags());
        resp.setAuthorName(post.getAuthor().getDisplayName());
        resp.setStatus(post.getStatus());
        resp.setCreatedAt(post.getCreatedAt());
        resp.setUpdatedAt(post.getUpdatedAt());
        return resp;
    }

    /**
     * 喵～ 从标题生成 URL 别名！
     * <p>
     * 规则如下：
     * <ol>
     *   <li>全部转为小写字母</li>
     *   <li>空格替换为连字符 -</li>
     *   <li>移除非字母数字和连字符的字符</li>
     *   <li>合并连续的连字符</li>
     *   <li>去掉首尾的连字符</li>
     * </ol>
     * 比如 "Hello World!! 喵～" → "hello-world-喵"
     *
     * @param title 文章标题
     * @return 生成的 URL 别名
     */
    private String generateSlug(String title) {
        if (title == null || title.isBlank()) {
            return "untitled";
        }
        String slug = title.toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5\\-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug.isEmpty() ? "untitled" : slug;
    }
}