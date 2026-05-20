package xl.xldevblog.main.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xl.xldevblog.main.dto.ApiResponse;
import xl.xldevblog.main.entity.PluginInfo;
import xl.xldevblog.main.plugin.PluginManager;
import xl.xldevblog.main.repository.PluginInfoRepository;
import xl.xldevblog.main.security.JwtUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 喵～ 插件管理控制器！
 * <p>
 * 提供插件的查询和重载接口，
 * 管理员可以通过这些接口来管理插件系统的运行状态。
 * <p>
 * 重载操作需要管理员权限验证，
 * 通过 Cookie 中的 X-Admin-Token 来确认身份喵～ (ฅ´ω`ฅ)
 */
@RestController
@RequestMapping("/api/plugins")
public class PluginRestController {

    private static final Logger log = LoggerFactory.getLogger(PluginRestController.class);

    /** 管理员 Token 在 Cookie 中的名称 */
    private static final String ADMIN_TOKEN_COOKIE = "X-Admin-Token";

    private final PluginInfoRepository pluginInfoRepository;
    private final PluginManager pluginManager;
    private final JwtUtil jwtUtil;

    public PluginRestController(PluginInfoRepository pluginInfoRepository, PluginManager pluginManager, JwtUtil jwtUtil) {
        this.pluginInfoRepository = pluginInfoRepository;
        this.pluginManager = pluginManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 喵～ 获取所有已加载的插件列表！
     * <p>
     * GET /api/plugins
     * <p>
     * 从 PluginInfoRepository 中查询所有 enabled=true 的插件，
     * 返回给前端展示当前已加载的插件信息～
     *
     * @return 已启用插件的列表
     */
    @GetMapping
    public ApiResponse getPlugins() {
        log.debug("喵～ 获取插件列表");
        List<PluginInfo> pluginList = pluginInfoRepository.findAll().stream()
            .filter(p -> Boolean.TRUE.equals(p.getEnabled()))
            .collect(Collectors.toList());
        return ApiResponse.ok(pluginList);
    }

    /**
     * 喵～ 重新加载所有插件！
     * <p>
     * POST /api/plugins/reload
     * <p>
     * 需要管理员权限，从 Cookie 中验证 X-Admin-Token。
     * 调用 PluginManager.reloadAll() 来卸载所有已加载的插件
     * 并重新扫描 plugins/ 目录进行加载喵～
     *
     * @param request HTTP 请求，用于提取 Cookie 中的管理员 Token
     * @return 操作结果消息
     */
    @PostMapping("/reload")
    public ApiResponse reloadPlugins(HttpServletRequest request) {
        if (!isAdminAuthenticated(request)) {
            return ApiResponse.error("身份验证失败，需要管理员权限喵～");
        }

        log.info("喵～ 管理员请求重新加载所有插件");
        pluginManager.reloadAll();
        return ApiResponse.ok("插件已重新加载喵～");
    }

    /**
     * 喵～ 从 Cookie 中验证管理员身份！
     * <p>
     * 遍历请求中的 Cookie，查找 X-Admin-Token，
     * 用 JwtUtil 验证 token 有效性并检查类型是否为 ADMIN。
     *
     * @param request HTTP 请求
     * @return true = 是管理员，false = 不是
     */
    private boolean isAdminAuthenticated(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ADMIN_TOKEN_COOKIE.equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (jwtUtil.validateToken(token) && "ADMIN".equals(jwtUtil.getTokenType(token))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}