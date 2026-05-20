package xl.xldevblog.main.plugin;

/**
 * 喵～ 路由定义类！
 * <p>
 * 用来描述一个插件提供的 HTTP 路由信息，
 * 包含访问路径、HTTP 方法以及对应的控制器。
 * <p>
 * 插件通过 Plugin.getRoutes() 返回一列这个对象，
 * 系统就会自动把路由注册到请求分发器里啦～
 */
public class RouteDefinition {

    /** 路由路径，比如 "/plugin/hello" 或 "/plugin/echo" */
    private String path;

    /** HTTP 方法，比如 "GET" 或 "POST"，不区分大小写 */
    private String method;

    /** 处理这个路由的控制器，用来处理请求并返回响应内容 */
    private PluginController controller;

    /**
     * 喵～ 无参构造器！
     * 给序列化框架用的，一般不需要手动调用～
     */
    public RouteDefinition() {}

    /**
     * 喵～ 全参构造器！
     * 推荐用这个来创建路由定义，
     * 一步到位设置好所有字段～
     *
     * @param path       路由路径，比如 "/plugin/hello"
     * @param method     HTTP 方法，比如 "GET" 或 "POST"
     * @param controller 处理这个路由的控制器实现
     */
    public RouteDefinition(String path, String method, PluginController controller) {
        this.path = path;
        this.method = method;
        this.controller = controller;
    }

    // ===== Getter / Setter =====

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public PluginController getController() { return controller; }
    public void setController(PluginController controller) { this.controller = controller; }
}