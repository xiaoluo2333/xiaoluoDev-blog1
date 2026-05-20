package xl.xldevblog.hello;

import xl.xldevblog.main.plugin.Plugin;
import xl.xldevblog.main.plugin.PluginController;
import xl.xldevblog.main.plugin.RouteDefinition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 喵～ HelloWorld 插件的主类！
 * <p>
 * 这是插件的入口点，实现了 Plugin 接口，
 * 就像 Bukkit 插件的 main class 一样～
 * 系统会通过 plugin.yml 里配置的 main 字段找到这个类并实例化它。
 * <p>
 * 这个插件会注册两个路由：
 * <ul>
 *   <li>GET /plugin/hello — 返回一个可爱的 HelloWorld 页面</li>
 *   <li>POST /plugin/hello/click — 处理按钮点击事件，返回 JSON 响应</li>
 * </ul>
 * <p>
 * 所有信息都会从 plugin.yml 里读取，
 * 但如果 yml 里没配置，系统也会 fallback 到插件类提供的信息喵～
 */
public class HelloWorldPlugin implements Plugin {

    /** 喵～ 插件唯一标识符，和 plugin.yml 里的 id 保持一致 */
    private static final String PLUGIN_ID = "hello-world";

    /** 喵～ 插件展示名称 */
    private static final String PLUGIN_NAME = "HelloWorld";

    /** 喵～ 插件版本号 */
    private static final String PLUGIN_VERSION = "1.0.0";

    /** 喵～ 插件作者 */
    private static final String PLUGIN_AUTHOR = "Xiaoluo";

    /** 喵～ 插件描述 */
    private static final String PLUGIN_DESCRIPTION = "一个简单的示例插件，用来演示插件系统怎么用喵～";

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getVersion() {
        return PLUGIN_VERSION;
    }

    @Override
    public String getAuthor() {
        return PLUGIN_AUTHOR;
    }

    @Override
    public String getDescription() {
        return PLUGIN_DESCRIPTION;
    }

    /**
     * 喵～ 插件加载时会调用这个方法！
     * <p>
     * 在这里可以做一些初始化工作，
     * 比如读取配置文件、建立连接等。
     * 这个插件比较简单，只需要打个招呼就好啦～
     */
    @Override
    public void onLoad() {
        System.out.println("喵～ HelloWorld 插件加载成功啦！(｀・ω・´)");
    }

    /**
     * 喵～ 插件卸载时会调用这个方法！
     * <p>
     * 在这里可以做一些清理工作，
     * 比如关闭连接、保存状态、释放资源等。
     * 我们只需要友好地道别就好啦～
     */
    @Override
    public void onUnload() {
        System.out.println("HelloWorld 插件卸载了，下次见喵～");
    }

    /**
     * 喵～ 返回插件要注册的路由列表！
     * <p>
     * 这个插件注册了两个路由：
     * <ol>
     *   <li>GET /plugin/hello — 返回 HTML 页面</li>
     *   <li>POST /plugin/hello/click — 处理点击事件，返回 JSON</li>
     * </ol>
     * <p>
     * 两个路由使用不同的控制器实例，
     * 通过 isClickEndpoint 标志来区分各自的行为喵～
     *
     * @return 包含两个路由定义的列表
     */
    @Override
    public List<RouteDefinition> getRoutes() {
        // 喵～ 创建两个控制器实例，用标志区分不同的端点
        // 这样两个路由可以共享同一个控制器类，但行为不同
        PluginController pageController = new HelloWorldController(false);
        PluginController clickController = new HelloWorldController(true);

        return List.of(
            new RouteDefinition("/plugin/hello", "GET", pageController),
            new RouteDefinition("/plugin/hello/click", "POST", clickController)
        );
    }
}