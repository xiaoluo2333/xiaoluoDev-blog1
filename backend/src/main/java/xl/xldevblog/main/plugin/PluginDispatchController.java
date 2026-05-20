package xl.xldevblog.main.plugin;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xl.xldevblog.main.dto.ApiResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * 喵～ 插件请求分发控制器！
 * <p>
 * 这是一个 @RestController，负责接收所有 /plugin/** 路径的 HTTP 请求，
 * 然后根据请求路径从 PluginManager 的路由表中查找对应的 RouteDefinition，
 * 最后调用插件控制器的 handle() 方法来处理请求并返回结果～
 * <p>
 * 相当于一个智能中转站，把请求准确无误地送到对应的插件手里！(｀・ω・´)
 */
@RestController
public class PluginDispatchController {

    /** 日志记录器，用来记录请求分发信息 */
    private static final Logger log = LoggerFactory.getLogger(PluginDispatchController.class);

    /** 插件管理器，用来获取路由表和已加载的插件信息 */
    private final PluginManager pluginManager;

    /**
     * 喵～ 构造注入！
     * Spring 会自动把 PluginManager 传进来～
     *
     * @param pluginManager 插件管理器实例
     */
    public PluginDispatchController(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    /**
     * 喵～ 处理所有 /plugin/** 的 HTTP 请求！
     * <p>
     * 这个方法的处理流程：
     * <ol>
     *   <li>从请求中提取完整的 URI 路径</li>
     *   <li>从 PluginManager 的路由表中查找匹配的路由定义</li>
     *   <li>验证 HTTP 方法是否匹配（GET / POST）</li>
     *   <li>收集请求参数（GET 的查询参数 + POST 的表单参数）</li>
     *   <li>调用对应的 PluginController.handle() 处理请求</li>
     *   <li>返回处理结果给客户端</li>
     * </ol>
     *
     * @param request HTTP 请求对象，用来获取路径、方法和参数
     * @return 处理结果，可以返回 String（HTML 页面）或其他对象（JSON 响应）
     */
    @RequestMapping("/plugin/**")
    public Object handlePluginRequest(HttpServletRequest request) {
        // ===== 获取请求的完整路径 =====
        String fullPath = request.getRequestURI();
        String contextPath = request.getContextPath();

        // 去掉 contextPath 部分，得到相对于应用的路径
        String path = fullPath;
        if (contextPath != null && !contextPath.isEmpty()) {
            path = fullPath.substring(contextPath.length());
        }

        log.debug("喵～ 收到插件请求: [{}] {}", request.getMethod(), path);

        // ===== 在路由表中查找匹配的路由定义 =====
        RouteDefinition route = pluginManager.getRoute(path);

        if (route == null) {
            // 没有找到对应的路由
            log.warn("喵呜… 找不到路径 {} 对应的插件路由", path);
            return ApiResponse.error("喵呜… 找不到这个插件路由: " + path);
        }

        // ===== 验证 HTTP 方法是否匹配 =====
        String requestMethod = request.getMethod().toUpperCase();
        String routeMethod = route.getMethod().toUpperCase();

        if (!routeMethod.equals(requestMethod)) {
            log.warn("喵呜… 路径 {} 的请求方法不匹配: 需要 {}，收到 {}",
                path, routeMethod, requestMethod);
            return ApiResponse.error(
                String.format("喵呜… HTTP 方法不匹配喵～ 需要 %s，收到 %s", routeMethod, requestMethod));
        }

        // ===== 收集请求参数 =====
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap != null) {
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String[] values = entry.getValue();
                if (values != null && values.length > 0) {
                    // 如果有多个值，只取第一个
                    params.put(entry.getKey(), values[0]);
                }
            }
        }

        // ===== 调用插件控制器处理请求 =====
        try {
            log.info("喵～ 分发请求到插件路由: [{}] {}", routeMethod, path);
            Object result = route.getController().handle(params);
            log.debug("喵～ 插件请求处理完成: [{}] {}", routeMethod, path);
            return result;
        } catch (Exception e) {
            log.error("喵呜… 插件处理请求时发生异常: [{}] {} - {}",
                requestMethod, path, e.getMessage(), e);
            return ApiResponse.error("插件处理请求时出错了喵～ " + e.getMessage());
        }
    }
}