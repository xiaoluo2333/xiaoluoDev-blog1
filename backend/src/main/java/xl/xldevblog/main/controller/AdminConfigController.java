package xl.xldevblog.main.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;
import xl.xldevblog.main.dto.ApiResponse;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 喵～ 系统配置管理控制器！
 * <p>
 * 提供系统配置的查看和更新接口，
 * 管理员可以通过这些接口来管理博客的运行配置。
 * <p>
 * GET 接口从 Environment 对象读取当前生效的配置，
 * PUT 接口将配置写入外部的 config/blog.yml 文件中，
 * 重启后即可生效喵～ (ฅ´ω`ฅ)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminConfigController {

    private static final Logger log = LoggerFactory.getLogger(AdminConfigController.class);

    /** 外部配置文件路径，相对于工作目录的 config/blog.yml */
    private static final String CONFIG_FILE_PATH = "config/blog.yml";

    /** 需要暴露给前端的配置键列表 */
    private static final List<String> CONFIG_KEYS = List.of(
        "cookie.user.default-expiration",
        "cookie.user.remember-me-expiration",
        "server.port",
        "file.upload-dir",
        "file.download-dir"
    );

    private final Environment environment;

    public AdminConfigController(Environment environment) {
        this.environment = environment;
    }

    /**
     * 喵～ 获取系统配置！
     * <p>
     * GET /api/admin/config
     * <p>
     * 从 Spring 的 Environment 对象中读取指定配置项，
     * 返回给前端展示当前运行时的配置状态喵～
     *
     * @return 当前系统配置的键值对 Map
     */
    @GetMapping("/config")
    public ApiResponse getConfig() {
        log.debug("喵～ 获取系统配置");
        Map<String, Object> config = new LinkedHashMap<>();
        for (String key : CONFIG_KEYS) {
            String value = environment.getProperty(key);
            config.put(key, value != null ? value : "");
        }
        return ApiResponse.ok(config);
    }

    /**
     * 喵～ 更新系统配置！
     * <p>
     * PUT /api/admin/config
     * <p>
     * 接收前端传来的配置 Map，将点号分隔的扁平键
     * 转换为嵌套的 YAML 结构，写入到外部的 config/blog.yml 文件中。
     * <p>
     * 注意：配置不会立即生效，需要重启后才能加载新的配置喵～
     *
     * @param configMap 配置项的键值对 Map
     * @return 操作结果消息
     */
    @PutMapping("/config")
    public ApiResponse updateConfig(@RequestBody Map<String, Object> configMap) {
        log.info("喵～ 更新系统配置: {}", configMap);

        // 将点号分隔的扁平键转换为嵌套的 Map 结构
        Map<String, Object> nestedConfig = convertToNestedMap(configMap);

        // 确保 config 目录存在
        try {
            Path configDir = Paths.get("config");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (IOException e) {
            log.error("喵呜… 创建 config 目录失败: {}", e.getMessage(), e);
            return ApiResponse.error("喵呜… 创建配置目录失败了喵～ " + e.getMessage());
        }

        // 用 SnakeYAML 将 Map 写入 YAML 文件
        Yaml yaml = new Yaml();
        try (FileWriter writer = new FileWriter(CONFIG_FILE_PATH)) {
            yaml.dump(nestedConfig, writer);
            log.info("喵～ 配置已保存到 {}", CONFIG_FILE_PATH);
        } catch (IOException e) {
            log.error("喵呜… 写入配置文件失败: {}", e.getMessage(), e);
            return ApiResponse.error("喵呜… 写入配置文件失败了喵～ " + e.getMessage());
        }

        return ApiResponse.ok("配置已更新喵～重启后生效");
    }

    /**
     * 喵～ 将扁平的键值对 Map 转换为嵌套的 Map 结构！
     * <p>
     * Spring Boot 的 YAML 配置使用嵌套结构（如 cookie.user.default-expiration），
     * 而前端传来的 Map 使用点号分隔的扁平键。
     * 这个方法将扁平键逐层拆解，构建出 YAML 所需的嵌套 Map。
     * <p>
     * 例如：
     * <pre>
     *   "server.port" = "8080"
     *   转换为：
     *   { "server": { "port": "8080" } }
     * </pre>
     *
     * @param flatMap 前端传来的扁平键值对 Map
     * @return 嵌套结构的 Map，适合 SnakeYAML 序列化
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToNestedMap(Map<String, Object> flatMap) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String[] parts = key.split("\\.");
            Map<String, Object> current = result;

            // 逐层创建嵌套结构，在最后一层设置值
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];
                if (!current.containsKey(part)) {
                    current.put(part, new LinkedHashMap<String, Object>());
                }
                Object next = current.get(part);
                if (next instanceof Map) {
                    current = (Map<String, Object>) next;
                } else {
                    // 如果该层不是 Map，覆盖为新的 Map
                    Map<String, Object> newMap = new LinkedHashMap<>();
                    current.put(part, newMap);
                    current = newMap;
                }
            }

            // 在最后一层设置值
            current.put(parts[parts.length - 1], value);
        }

        return result;
    }
}