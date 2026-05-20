package xl.xldevblog.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 喵～ 博客系统启动引导类！
 * <p>
 * 在 Spring Boot 正式启动之前执行前置准备工作，
 * 负责初始化运行环境、验证配置文件、扫描插件等。
 * <p>
 * 启动流程分为四个阶段：
 * <ol>
 *   <li>初始化运行环境——创建必要的目录结构</li>
 *   <li>验证配置文件——检查 config/blog.yml 是否存在且格式正确</li>
 *   <li>扫描并验证插件——检查 plugins/ 下的 JAR 是否有效</li>
 *   <li>启动 Spring Boot——前置准备完成后拉起主应用</li>
 * </ol>
 */
public class BlogBootstrap {

    /** 日志记录器 */
    private static final Logger log = LogManager.getLogger(BlogBootstrap.class);

    /** 博客运行所需的目录列表 */
    private static final String[] REQUIRED_DIRS = {
        "config",
        "plugins",
        "uploads",
        "downloads",
        "logs"
    };

    /** 外部配置文件的路径，相对于工作目录 */
    private static final String CONFIG_FILE = "config/blog.yml";

    /** 命令行参数：重新生成配置文件 */
    private static final String RECRAFT_ARG = "-recraft_config";

    /** 默认配置模板（YAML 格式），当配置文件不存在时自动生成 */
    private static final String DEFAULT_CONFIG_TEMPLATE =
        "# 喵～ XiaoluoDev Blog 配置文件\n" +
        "# 修改完配置后直接启动就好啦！\n" +
        "\n" +
        "server:\n" +
        "  port: 8080\n" +
        "\n" +
        "spring:\n" +
        "  datasource:\n" +
        "    url: jdbc:mysql://localhost:3306/xiaoluoDev_blog_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai\n" +
        "    username: root\n" +
        "    password: 35433123f\n" +
        "    driver-class-name: com.mysql.cj.jdbc.Driver\n" +
        "  jpa:\n" +
        "    hibernate:\n" +
        "      ddl-auto: update\n" +
        "    show-sql: false\n" +
        "\n" +
        "cookie:\n" +
        "  user:\n" +
        "    cookie-name: X-User-Token\n" +
        "    default-expiration: 86400\n" +
        "    remember-me-expiration: 604800\n" +
        "  admin:\n" +
        "    cookie-name: X-Admin-Token\n" +
        "\n" +
        "plugin:\n" +
        "  directory: ./plugins\n" +
        "  scan-interval: 5000\n";

    /**
     * 配置文件验证结果枚举。
     * <ul>
     *   <li>VALID — 配置文件存在且格式正确</li>
     *   <li>NOT_FOUND — 未找到配置文件</li>
     *   <li>INVALID — 配置文件存在但格式有误</li>
     * </ul>
     */
    enum ConfigValidationResult {
        VALID,
        NOT_FOUND,
        INVALID
    }

    /**
     * 博客系统启动入口。
     * <p>
     * 按四个阶段依次执行：初始化目录 → 验证配置 → 扫描插件 → 启动 Spring Boot。
     * 支持 {@code -recraft_config} 参数，在配置文件损坏时可自动备份并重新生成。
     *
     * @param args 命令行参数，支持 {@code -recraft_config} 选项
     */
    public static void main(String[] args) {
        boolean recraftConfig = false;
        List<String> filteredArgs = new ArrayList<>();
        for (String arg : args) {
            if (RECRAFT_ARG.equals(arg)) {
                recraftConfig = true;
            } else {
                filteredArgs.add(arg);
            }
        }
        String[] springArgs = filteredArgs.toArray(new String[0]);

        log.info("喵～ BlogBootstrap 启动中... (｀・ω・´)");
        log.info("========== 阶段一：初始化运行环境 ==========");

        if (!initializeDirectories()) {
            log.error("目录初始化失败，博客无法启动喵... 请检查文件系统权限！(´;ω;`)");
            System.exit(1);
        }
        log.info("运行环境目录初始化完成喵～");

        log.info("========== 阶段二：验证配置文件 ==========");

        switch (validateConfig()) {
            case NOT_FOUND:
                log.warn("外部配置文件 config/blog.yml 不存在，正在创建默认配置喵～");
                createDefaultConfig();
                log.info("默认配置文件已创建：{}", new File(CONFIG_FILE).getAbsolutePath());
                log.warn("请修改 config/blog.yml 中的数据库连接信息后再启动喵～");
                break;

            case INVALID:
                log.error("配置文件验证失败喵！(´;ω;`) 请根据以上错误信息修复配置文件。");
                log.error("提示：如果希望自动备份错误的配置并重新生成，请使用 {} 参数启动喵～", RECRAFT_ARG);
                if (recraftConfig) {
                    log.info("检测到 {} 参数，正在备份并重新创建配置文件喵～", RECRAFT_ARG);
                    backupAndRecraftConfig();
                } else {
                    log.warn("将使用 JAR 内置的 application.properties 配置继续启动喵～");
                }
                break;

            case VALID:
                log.info("配置文件验证通过喵～");
                break;
        }

        log.info("========== 阶段三：扫描并验证插件 ==========");

        int pluginCount = scanAndValidatePlugins();
        log.info("共发现 {} 个插件 JAR 包", pluginCount);

        log.info("========== 阶段四：启动 Spring Boot ==========");
        log.info("前置准备全部完成，正在拉起 Spring Boot 主类喵～ 请稍候...");

        try {
            SpringApplication.run(BlogApplication.class, springArgs);
        } catch (Exception e) {
            log.error("Spring Boot 启动失败喵！请检查配置和数据库连接：{}", e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * 初始化博客运行所需的目录结构。
     * <p>
     * 检查 config、plugins、uploads、downloads、logs 目录是否存在，
     * 不存在则自动创建。支持 Windows 和 Linux 双平台。
     *
     * @return true = 所有目录就绪，false = 有目录创建失败
     */
    private static boolean initializeDirectories() {
        boolean allSuccess = true;
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osName.contains("win");
        log.debug("当前操作系统：{}（{}）", System.getProperty("os.name"), isWindows ? "Windows" : "Linux/Unix");

        for (String dirName : REQUIRED_DIRS) {
            Path dirPath = Paths.get(dirName).toAbsolutePath();

            if (Files.exists(dirPath)) {
                if (Files.isDirectory(dirPath)) {
                    log.debug("目录已存在：{}", dirPath);
                } else {
                    log.error("路径存在但不是目录喵！请检查：{}", dirPath);
                    allSuccess = false;
                }
                continue;
            }

            try {
                Files.createDirectories(dirPath);
                log.info("创建目录：{}", dirPath);
            } catch (IOException e) {
                log.error("创建目录失败喵！{} - {}", dirPath, e.getMessage());
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    /**
     * 创建默认配置文件。
     * <p>
     * 将 DEFAULT_CONFIG_TEMPLATE 写入 config/blog.yml，
     * 如果 config 目录不存在也会自动创建。
     */
    private static void createDefaultConfig() {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write(DEFAULT_CONFIG_TEMPLATE);
            writer.flush();
        } catch (IOException e) {
            log.error("创建默认配置文件失败喵！{} - {}", CONFIG_FILE, e.getMessage());
        }
    }

    /**
     * 备份当前配置文件并重新生成默认配置。
     * <p>
     * 当配置文件格式错误且用户指定了 {@code -recraft_config} 参数时调用。
     * 备份文件命名为 blog.yml.error_bak（如已存在则追加 .1, .2, ...）。
     * 备份完成后调用 createDefaultConfig() 生成新的默认配置。
     */
    private static void backupAndRecraftConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            log.warn("配置文件不存在，无需备份，直接创建默认配置喵～");
            createDefaultConfig();
            return;
        }

        File backupFile = new File(CONFIG_FILE + ".error_bak");
        int counter = 0;
        while (backupFile.exists()) {
            counter++;
            backupFile = new File(CONFIG_FILE + ".error_bak." + counter);
        }

        try {
            Files.move(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("已备份错误的配置文件为：{}", backupFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("备份配置文件失败喵！{} - {}", e.getMessage());
            return;
        }

        createDefaultConfig();
        log.info("已重新创建默认配置文件：{}", new File(CONFIG_FILE).getAbsolutePath());
        log.warn("请修改 config/blog.yml 中的数据库连接信息后再启动喵～");
    }

    /**
     * 验证外部配置文件的格式和完整性。
     * <p>
     * 依次检查：文件是否存在、是否为空、YAML 语法是否正确、
     * 根节点是否为 Map 结构、必要配置项（server、spring.datasource 等）是否缺失。
     *
     * @return ConfigValidationResult 验证结果枚举
     */
    @SuppressWarnings("unchecked")
    private static ConfigValidationResult validateConfig() {
        File configFile = new File(CONFIG_FILE);

        if (!configFile.exists()) {
            log.warn("未找到外部配置文件：{}", configFile.getAbsolutePath());
            return ConfigValidationResult.NOT_FOUND;
        }

        if (!configFile.isFile()) {
            log.error("config/blog.yml 不是一个有效的文件喵！路径：{}", configFile.getAbsolutePath());
            return ConfigValidationResult.INVALID;
        }

        if (configFile.length() == 0) {
            log.error("配置文件内容为空喵！请检查：{}", configFile.getAbsolutePath());
            return ConfigValidationResult.INVALID;
        }

        try (InputStream input = new FileInputStream(configFile)) {
            Yaml yaml = new Yaml();
            Object parsed;

            try {
                parsed = yaml.load(input);
            } catch (YAMLException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String trace = sw.toString();

                String positionInfo = "";
                String msg = e.getMessage();
                if (msg != null) {
                    int lineIdx = msg.indexOf("line=");
                    int colIdx = msg.indexOf("column=");
                    if (lineIdx >= 0) {
                        int lineEnd = msg.indexOf(",", lineIdx);
                        positionInfo = msg.substring(lineIdx, lineEnd >= 0 ? lineEnd : msg.length());
                    }
                    if (colIdx >= 0) {
                        int colEnd = msg.indexOf(",", colIdx);
                        positionInfo += ", " + (colIdx > 0 ? msg.substring(colIdx, colEnd >= 0 ? colEnd : msg.length()) : "");
                    }
                }

                log.error("配置文件 YAML 语法错误喵！{}", positionInfo.isEmpty() ? "" : "位置：" + positionInfo);
                log.error("错误详情：{}", e.getMessage());
                log.debug("YAML 解析异常堆栈：\n{}", trace);
                return ConfigValidationResult.INVALID;
            }

            if (parsed == null) {
                log.error("配置文件内容为空喵！路径：{}", configFile.getAbsolutePath());
                return ConfigValidationResult.INVALID;
            }

            if (!(parsed instanceof Map)) {
                log.error("配置文件格式不正确喵！根节点应该是一个 Map 结构（键值对），但得到了：{}", parsed.getClass().getSimpleName());
                log.error("请检查 config/blog.yml 是否以正确的 YAML 格式编写（例如以 \"server:\" 开头）");
                return ConfigValidationResult.INVALID;
            }

            Map<String, Object> config = (Map<String, Object>) parsed;
            List<String> missingKeys = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            if (!config.containsKey("server")) {
                missingKeys.add("server");
            }
            if (!config.containsKey("spring")) {
                missingKeys.add("spring");
            }

            if (config.containsKey("spring")) {
                Object springVal = config.get("spring");
                if (springVal instanceof Map) {
                    Map<String, Object> springMap = (Map<String, Object>) springVal;
                    if (!springMap.containsKey("datasource")) {
                        missingKeys.add("spring.datasource");
                    }
                } else {
                    warnings.add("spring 配置项应该是一个 Map 结构");
                }
            }

            if (config.containsKey("cookie")) {
                Object cookieVal = config.get("cookie");
                if (cookieVal instanceof Map) {
                    Map<String, Object> cookieMap = (Map<String, Object>) cookieVal;
                    if (!cookieMap.containsKey("user")) {
                        warnings.add("缺少 cookie.user 配置，将使用默认的 Cookie 名称和过期时间");
                    }
                    if (!cookieMap.containsKey("admin")) {
                        warnings.add("缺少 cookie.admin 配置，将使用默认的管理员 Cookie 配置");
                    }
                }
            }

            if (config.containsKey("plugin")) {
                Object pluginVal = config.get("plugin");
                if (pluginVal instanceof Map) {
                    Map<String, Object> pluginMap = (Map<String, Object>) pluginVal;
                    if (!pluginMap.containsKey("directory")) {
                        warnings.add("缺少 plugin.directory 配置，将使用默认的 ./plugins 目录");
                    }
                }
            }

            if (!missingKeys.isEmpty()) {
                log.error("配置文件中缺少以下必要的配置项喵：");
                for (String key : missingKeys) {
                    log.error("   - {}", key);
                }
                log.error("请根据默认配置模板补全这些配置项喵～");
            }

            for (String warn : warnings) {
                log.warn("配置建议：{}", warn);
            }

            if (!missingKeys.isEmpty()) {
                return ConfigValidationResult.INVALID;
            }

            log.info("配置文件格式验证通过：{}", configFile.getAbsolutePath());
            return ConfigValidationResult.VALID;

        } catch (IOException e) {
            log.error("读取配置文件时发生 IO 错误喵！{} - {}", configFile.getAbsolutePath(), e.getMessage());
            return ConfigValidationResult.INVALID;
        }
    }

    /**
     * 扫描 plugins/ 目录并验证所有 JAR 插件。
     * <p>
     * 遍历目录下所有 .jar 文件，逐个调用 validatePluginJar 检查
     * plugin.yml 是否存在、必填字段是否完整、主类是否可以加载。
     *
     * @return 有效的插件数量
     */
    private static int scanAndValidatePlugins() {
        File pluginsDir = new File("plugins");
        int validCount = 0;
        int invalidCount = 0;

        if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
            log.warn("plugins/ 目录不存在，跳过插件扫描喵～");
            return 0;
        }

        File[] jarFiles = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            log.info("plugins/ 目录中没有找到 JAR 文件喵～");
            return 0;
        }

        for (File jarFile : jarFiles) {
            if (validatePluginJar(jarFile)) {
                log.info("[有效] 插件 JAR：{}", jarFile.getName());
                validCount++;
            } else {
                log.warn("[无效] 插件 JAR 缺少 plugin.yml：{}", jarFile.getName());
                invalidCount++;
            }
        }

        log.info("插件扫描完成：{} 个有效，{} 个无效", validCount, invalidCount);
        return validCount;
    }

    /**
     * 验证单个插件 JAR 文件的有效性。
     * <p>
     * 打开 JAR 文件，检查其中是否包含 plugin.yml，
     * 解析 plugin.yml 并验证 main 和 id 字段是否完整，
     * 尝试加载主类确认类文件是否存在。
     *
     * @param jarFile 要验证的插件 JAR 文件
     * @return true = 插件有效，false = 插件无效
     */
    private static boolean validatePluginJar(File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry pluginYmlEntry = jar.getJarEntry("plugin.yml");
            if (pluginYmlEntry == null) {
                return false;
            }

            try (InputStream input = jar.getInputStream(pluginYmlEntry)) {
                Yaml yaml = new Yaml();
                Object parsed = yaml.load(input);

                if (parsed instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> pluginInfo = (Map<String, Object>) parsed;

                    String mainClass = (String) pluginInfo.get("main");
                    String pluginId = (String) pluginInfo.get("id");

                    if (mainClass == null || mainClass.isEmpty()) {
                        log.warn("  插件 {} 的 plugin.yml 缺少 'main' 字段喵～", jarFile.getName());
                    }
                    if (pluginId == null || pluginId.isEmpty()) {
                        log.warn("  插件 {} 的 plugin.yml 缺少 'id' 字段喵～", jarFile.getName());
                    }

                    if (mainClass != null && !mainClass.isEmpty()) {
                        try {
                            java.net.URLClassLoader loader = new java.net.URLClassLoader(
                                new java.net.URL[]{jarFile.toURI().toURL()},
                                BlogBootstrap.class.getClassLoader()
                            );
                            Class<?> clazz = Class.forName(mainClass, false, loader);
                            log.debug("  插件主类验证通过：{}", clazz.getName());
                            loader.close();
                        } catch (ClassNotFoundException e) {
                            log.warn("  插件 {} 的主类 {} 未找到喵～ {}", jarFile.getName(), mainClass, e.getMessage());
                        }
                    }

                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            log.warn("  读取插件 JAR 失败：{} - {}", jarFile.getName(), e.getMessage());
            return false;
        }
    }
}