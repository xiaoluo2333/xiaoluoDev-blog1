package xl.xldevblog.main.plugin;

import java.util.Map;

/**
 * 喵～ 插件控制器接口！
 * <p>
 * 插件处理 HTTP 请求的地方～
 * 实现这个接口来处理来自客户端的请求，
 * 返回页面内容（String）或者 ModelAndView 给前端。
 * <p>
 * 插件作者只需要关心怎么处理 params 里的参数，
 * 其他的路由匹配、请求分发都由系统自动完成喵！
 */
public interface PluginController {

    /**
     * 喵～ 处理 HTTP 请求！
     * <p>
     * 当有请求匹配到插件注册的路由时，
     * 系统会调用这个方法，把请求参数传进来。
     * <p>
     * 返回结果可以是：
     * <ul>
     *   <li>String - 直接返回 HTML 页面内容</li>
     *   <li>其他 Object - 会作为 JSON 响应返回给客户端</li>
     * </ul>
     *
     * @param params 请求参数字典，key 是参数名，value 是参数值
     *               对于 GET 请求是查询参数，POST 请求是表单参数
     * @return 处理结果，可以是 String（页面内容）或其他对象（JSON 响应）
     */
    Object handle(Map<String, String> params);
}