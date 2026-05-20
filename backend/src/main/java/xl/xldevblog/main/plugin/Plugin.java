package xl.xldevblog.main.plugin;

import java.util.List;

/**
 * 喵～ 插件核心接口！
 * <p>
 * 所有插件都必须实现这个接口，
 * 就像 Bukkit 的 Plugin 接口一样喵～
 * <p>
 * 插件作者需要实现这些方法来告诉系统：
 * 你是谁、叫什么、有什么功能，
 * 以及在加载和卸载时要做什么～
 */
public interface Plugin {

    /**
     * 喵～ 获取插件的唯一标识符！
     * 这个 ID 在整个系统中是唯一的，
     * 比如 "hello-world" 或者 "my-awesome-plugin"
     *
     * @return 插件唯一标识符，不能为 null 喵
     */
    String getId();

    /**
     * 喵～ 获取插件的展示名称！
     * 这是给人看的名字，会显示在插件列表里，
     * 比如 "HelloWorld Plugin" 之类的～
     *
     * @return 插件展示名称
     */
    String getName();

    /**
     * 喵～ 获取插件版本号！
     * 建议遵循语义化版本规范，
     * 比如 "1.0.0" 或 "2.3.1-beta"
     *
     * @return 版本号字符串
     */
    String getVersion();

    /**
     * 喵～ 获取插件作者！
     * 就是开发这个插件的伟大的人～
     * 会显示在插件信息里让大家知道是谁做的
     *
     * @return 作者名称
     */
    String getAuthor();

    /**
     * 喵～ 获取插件描述！
     * 简单介绍这个插件是做什么用的，
     * 让用户一眼就能知道插件功能
     *
     * @return 插件描述文本
     */
    String getDescription();

    /**
     * 喵～ 插件加载时调用！
     * 当插件被成功加载到系统中时，
     * 会调用这个方法来做初始化工作～
     * 比如创建配置文件、建立数据库连接等
     */
    void onLoad();

    /**
     * 喵～ 插件卸载时调用！
     * 当插件被卸载或系统关闭时，
     * 会调用这个方法来做清理工作～
     * 比如关闭连接、保存状态、释放资源等
     */
    void onUnload();

    /**
     * 喵～ 返回插件要注册的路由列表！
     * 插件通过这个方法告诉系统它提供了哪些 HTTP 接口，
     * 系统会自动把这些路由注册到请求分发器中～
     * <p>
     * 每个 RouteDefinition 包含路径、HTTP 方法和对应的控制器，
     * 比如可以注册一个 GET /plugin/hello 的路由来提供问候服务
     *
     * @return 路由定义列表，如果没有路由可以返回空列表
     */
    List<RouteDefinition> getRoutes();
}