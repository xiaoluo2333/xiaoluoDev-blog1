package xl.xldevblog.hello;

import xl.xldevblog.main.plugin.PluginController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 喵～ HelloWorld 插件的请求处理器！
 * <p>
 * 实现了 PluginController 接口，用来处理 HTTP 请求。
 * 这个控制器比较特别，它同时处理两个路由：
 * <ul>
 *   <li>GET /plugin/hello — 返回 HTML 页面内容</li>
 *   <li>POST /plugin/hello/click — 返回 JSON 响应</li>
 * </ul>
 * <p>
 * 通过构造时传入的 isClickEndpoint 标志来区分行为。
 * 这是为了演示插件控制器可以根据不同的注册方式表现出不同的行为喵～
 */
public class HelloWorldController implements PluginController {

    /** 喵～ 标志位：true 表示这是点击端点（返回 JSON），false 表示页面端点（返回 HTML） */
    private final boolean isClickEndpoint;

    /**
     * 喵～ 构造器！
     *
     * @param isClickEndpoint 如果为 true，则这个控制器实例处理点击请求（返回 JSON）；
     *                        如果为 false，则处理页面请求（返回 HTML）
     */
    public HelloWorldController(boolean isClickEndpoint) {
        this.isClickEndpoint = isClickEndpoint;
    }

    /**
     * 喵～ 处理 HTTP 请求！
     * <p>
     * 根据 isClickEndpoint 标志决定返回什么内容：
     * <ul>
     *   <li>如果这是点击端点（POST /plugin/hello/click）：
     *     <ul>
     *       <li>在控制台输出点击日志</li>
     *       <li>返回 JSON 字符串给前端</li>
     *     </ul>
     *   </li>
     *   <li>如果这是页面端点（GET /plugin/hello）：
     *     <ul>
     *       <li>从 JAR 包内加载 /templates/hello.html 模板文件</li>
     *       <li>返回 HTML 页面内容给前端</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @param params 请求参数，key 是参数名，value 是参数值
     *               对于 GET 请求是查询参数，POST 请求是表单参数
     * @return 如果是点击端点返回 JSON 字符串，否则返回 HTML 页面内容
     */
    @Override
    public Object handle(Map<String, String> params) {
        // 喵～ 判断当前是哪个端点
        if (isClickEndpoint) {
            // ===== 处理 POST /plugin/hello/click =====
            // 在服务端控制台输出点击日志
            System.out.println("[HelloWorld] Button clicked! 📝");

            // 返回 JSON 响应给前端
            return "{\"success\": true, \"message\": \"喵～ 收到消息啦！\"}";
        } else {
            // ===== 处理 GET /plugin/hello =====
            // 从 JAR 包中加载 HTML 模板文件
            return loadHtmlTemplate();
        }
    }

    /**
     * 喵～ 从 JAR 包内加载 HTML 模板文件！
     * <p>
     * {@code /templates/hello.html} 文件位于 JAR 包的根目录下的 templates 文件夹中，
     * 通过 ClassLoader 的 getResourceAsStream 方法加载。
     * <p>
     * 如果加载失败，会返回一个备用的 HTML 页面，保证用户能看到内容喵～
     *
     * @return HTML 页面内容字符串
     */
    private String loadHtmlTemplate() {
        // 喵～ 尝试从 JAR 包中加载模板文件
        InputStream inputStream = getClass().getResourceAsStream("/templates/hello.html");

        if (inputStream != null) {
            // 喵～ 成功找到模板文件，读取内容并返回
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            } catch (IOException e) {
                // 喵呜… 读取文件时出错了，打印错误日志
                System.err.println("[HelloWorld] 读取模板文件失败喵～ " + e.getMessage());
            }
        } else {
            // 喵呜… 在 JAR 包里找不到模板文件
            System.err.println("[HelloWorld] 找不到 /templates/hello.html 文件喵～");
        }

        // 喵～ 加载失败时返回一个简单的备用页面
        return buildFallbackHtml();
    }

    /**
     * 喵～ 构建备用 HTML 页面！
     * <p>
     * 当模板文件加载失败时使用，
     * 保证用户至少能看到一个正常的页面，
     * 而不是看到白屏或者错误信息喵～
     *
     * @return 备用的 HTML 页面内容
     */
    private String buildFallbackHtml() {
        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>喵～ HelloWorld 插件</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        min-height: 100vh;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
                        background: linear-gradient(135deg, #fce4ec, #f48fb1);
                        padding: 20px;
                    }
                    .card {
                        background: rgba(255,255,255,0.95);
                        border-radius: 24px;
                        padding: 48px 40px;
                        max-width: 480px;
                        width: 100%;
                        text-align: center;
                        box-shadow: 0 20px 60px rgba(233,30,99,0.15);
                    }
                    h1 { font-size: 28px; color: #c2185b; margin-bottom: 12px; }
                    p { color: #888; margin-bottom: 24px; line-height: 1.6; }
                    .icon { font-size: 72px; display: block; margin-bottom: 20px; }
                    .btn {
                        display: inline-block;
                        padding: 14px 40px;
                        font-size: 16px;
                        font-weight: 600;
                        color: #fff;
                        background: linear-gradient(135deg, #ec407a, #d81b60);
                        border: none;
                        border-radius: 50px;
                        cursor: pointer;
                        box-shadow: 0 6px 20px rgba(233,30,99,0.3);
                    }
                    .btn:hover {
                        transform: translateY(-3px);
                        box-shadow: 0 10px 28px rgba(233,30,99,0.4);
                    }
                    .msg {
                        margin-top: 24px;
                        padding: 16px 20px;
                        border-radius: 12px;
                        color: #c2185b;
                        background: #fce4ec;
                        min-height: 20px;
                        opacity: 0;
                        transition: all 0.3s ease;
                    }
                    .msg.show { opacity: 1; }
                    .footer { margin-top: 32px; font-size: 12px; color: #bbb; }
                </style>
            </head>
            <body>
                <div class="card">
                    <span class="icon">🐱</span>
                    <h1>喵～ Hello World！(｀・ω・´)</h1>
                    <p>欢迎来到 HelloWorld 插件～<br>（这是备用页面，模板加载失败啦喵）</p>
                    <button class="btn" id="helloBtn">点我测试插件功能</button>
                    <div class="msg" id="messageArea"></div>
                    <div class="footer">由 ♥ Xiaoluo 制作 · HelloWorld Plugin v1.0.0</div>
                </div>
                <script>
                    document.getElementById('helloBtn').addEventListener('click', function() {
                        console.log('Hello World from Plugin! 🎉');
                        var xhr = new XMLHttpRequest();
                        xhr.open('POST', '/plugin/hello/click', true);
                        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                        xhr.onload = function() {
                            var area = document.getElementById('messageArea');
                            if (xhr.status === 200) {
                                try {
                                    var r = JSON.parse(xhr.responseText);
                                    area.textContent = r.success ? (r.message || '后端已收到消息喵～') : '出错啦：' + r.message;
                                } catch(e) { area.textContent = '后端已收到消息喵～'; }
                            } else {
                                area.textContent = '喵呜… 请求失败了 (´;ω;｀)';
                            }
                            area.classList.add('show');
                        };
                        xhr.onerror = function() {
                            var area = document.getElementById('messageArea');
                            area.textContent = '喵呜… 网络连接失败了 (´;ω;｀)';
                            area.classList.add('show');
                        };
                        xhr.send();
                    });
                </script>
            </body>
            </html>
            """;
    }
}