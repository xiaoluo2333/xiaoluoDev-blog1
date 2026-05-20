package xl.xldevblog.main.controller;

import xl.xldevblog.main.dto.ApiResponse;
import xl.xldevblog.main.dto.PostRequest;
import xl.xldevblog.main.dto.PostResponse;
import xl.xldevblog.main.security.JwtUtil;
import xl.xldevblog.main.service.PostService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 喵～ 博客文章控制器！
 * <p>
 * 提供文章相关的所有 RESTful API，
 * 包括公开的文章浏览和管理员的文章管理功能。
 * <p>
 * 管理员操作需要通过 X-Admin-Token Cookie 验证身份，
 * 普通用户只能浏览已发布的文章喵～ (ฅ´ω`ฅ)
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    /** 管理员 Token 在 Cookie 中的名称，和 AdminJwtAuthFilter 保持一致喵 */
    private static final String ADMIN_TOKEN_COOKIE = "X-Admin-Token";

    private final PostService postService;
    private final JwtUtil jwtUtil;

    public PostController(PostService postService, JwtUtil jwtUtil) {
        this.postService = postService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 喵～ 获取已发布的文章列表！
     * <p>
     * 公开接口，不需要登录就能访问。
     * 只返回 status = PUBLISHED 的文章，
     * 按时间倒序排列，最新的在最前面。
     *
     * @param page 页码，从 0 开始，默认 0
     * @param size 每页条数，默认 10
     * @return 分页后的已发布文章列表
     */
    @GetMapping
    public ApiResponse getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostResponse> postPage = postService.getPosts(page, size);
        return ApiResponse.ok(postPage);
    }

    /**
     * 喵～ 管理员获取所有文章（含草稿）！
     * <p>
     * 需要管理员权限，从 Cookie 中验证 X-Admin-Token。
     * 不限制文章状态，草稿和已发布的都会返回。
     * 如果身份验证失败，返回错误信息喵～
     *
     * @param page    页码，从 0 开始，默认 0
     * @param size    每页条数，默认 10
     * @param request HTTP 请求，用于提取 Cookie
     * @return 分页后的所有文章列表，或错误信息
     */
    @GetMapping("/admin/all")
    public ApiResponse getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long adminId = getAdminIdFromCookie(request);
        if (adminId == null) {
            return ApiResponse.error("身份验证失败，请先登录管理员账号喵～");
        }
        Page<PostResponse> postPage = postService.getAllPosts(page, size);
        return ApiResponse.ok(postPage);
    }

    /**
     * 喵～ 根据 slug 获取单篇文章！
     * <p>
     * 公开接口，通过 URL 别名查找文章。
     * 比如访问 /api/posts/hello-world 就能找到 slug 为 hello-world 的文章。
     * 没找到的话返回错误提示喵～
     *
     * @param slug 文章的 URL 别名
     * @return 文章详情，或错误信息
     */
    @GetMapping("/{slug}")
    public ApiResponse getPostBySlug(@PathVariable String slug) {
        PostResponse post = postService.getPostBySlug(slug);
        if (post == null) {
            return ApiResponse.error("文章没找到喵～");
        }
        return ApiResponse.ok(post);
    }

    /**
     * 喵～ 创建新文章！
     * <p>
     * 需要管理员权限，从 Cookie 中验证 X-Admin-Token。
     * 创建成功后返回完整的文章信息。
     * 如果没传 slug，后端会自动从标题生成喵～
     *
     * @param req     文章创建请求 DTO
     * @param request HTTP 请求，用于提取 Cookie 中的管理员信息
     * @return 创建成功的文章信息，或错误信息
     */
    @PostMapping
    public ApiResponse createPost(@RequestBody PostRequest req, HttpServletRequest request) {
        Long adminId = getAdminIdFromCookie(request);
        if (adminId == null) {
            return ApiResponse.error("身份验证失败，请先登录管理员账号喵～");
        }
        try {
            PostResponse post = postService.createPost(req, adminId);
            return ApiResponse.ok(post);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 喵～ 更新文章！
     * <p>
     * 需要管理员权限，从 Cookie 中验证 X-Admin-Token。
     * 根据文章 ID 更新标题、内容、摘要等信息。
     * 注意 slug 不会被更新，因为旧链接会失效喵～
     *
     * @param id      文章 ID
     * @param req     文章更新请求 DTO
     * @param request HTTP 请求，用于提取 Cookie 中的管理员信息
     * @return 更新后的文章信息，或错误信息
     */
    @PutMapping("/{id}")
    public ApiResponse updatePost(@PathVariable Long id, @RequestBody PostRequest req, HttpServletRequest request) {
        Long adminId = getAdminIdFromCookie(request);
        if (adminId == null) {
            return ApiResponse.error("身份验证失败，请先登录管理员账号喵～");
        }
        try {
            PostResponse post = postService.updatePost(id, req);
            return ApiResponse.ok(post);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 喵～ 删除文章！
     * <p>
     * 需要管理员权限，从 Cookie 中验证 X-Admin-Token。
     * 根据文章 ID 永久删除，删除后无法恢复，请谨慎操作喵～
     *
     * @param id      要删除的文章 ID
     * @param request HTTP 请求，用于提取 Cookie 中的管理员信息
     * @return 删除成功的提示信息，或错误信息
     */
    @DeleteMapping("/{id}")
    public ApiResponse deletePost(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = getAdminIdFromCookie(request);
        if (adminId == null) {
            return ApiResponse.error("身份验证失败，请先登录管理员账号喵～");
        }
        try {
            postService.deletePost(id);
            return ApiResponse.ok("已删除喵～");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 喵～ 从 Cookie 中提取并验证管理员身份！
     * <p>
     * 从请求的 Cookie 中查找 X-Admin-Token，
     * 验证 token 的有效性并检查类型是否为 ADMIN。
     * 双重验证确保只有真正的管理员能通过喵～
     *
     * @param request HTTP 请求
     * @return 管理员 ID，验证失败则返回 null
     */
    private Long getAdminIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ADMIN_TOKEN_COOKIE.equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (jwtUtil.validateToken(token) && "ADMIN".equals(jwtUtil.getTokenType(token))) {
                        return jwtUtil.getUserIdFromToken(token);
                    }
                }
            }
        }
        return null;
    }
}