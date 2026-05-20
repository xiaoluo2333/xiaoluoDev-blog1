package xl.xldevblog.main.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import xl.xldevblog.main.entity.PluginInfo;
import xl.xldevblog.main.repository.PluginInfoRepository;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 喵～ 插件管理器！
 * <p>
 * 这是插件系统的核心服务，负责插件的加载、卸载、重载和生命周期管理～
 * 它会扫描 plugins/ 目录下的 JAR 文件，读取 plugin.yml 配置，
 * 实例化插件主类，注册路由，并把插件信息保存到数据库中。
 * <p>
 * 还带有一个定时任务，每 5 秒扫描一次插件目录，
 * 自动发现新插件或检测插件变更，非常智能喵！(｀・ω・´)
 */
@Service
public class PluginManager {

    /** 日志记录器，用来输出插件加载和运行时的各种信息 */
    private static final Logger log = LoggerFactory.getLogger(PluginManager.class);

    /** 插件存放目录，相对于工作目录的 plugins/ 文件夹 */
    private static final Path PLUGINS_DIR = Paths.get("plugins");

    /** 当前已加载的插件实例映射，key 是插件 ID */
    private final Map<String, Plugin> loadedPlugins = new ConcurrentHashMap<>();

    /** 已注册的路由表，key 是路由路径（如 "/plugin/hello"），value 是路由定义 */
    private final Map<String, RouteDefinition> routes = new ConcurrentHashMap<>();

    /** 每个插件对应的 ClassLoader，key 是插件 ID */
    private final Map<String, PluginClassLoader> classLoaders = new ConcurrentHashMap<>();

    /** 每个插件对应的 JAR 文件路径，key 是插件 ID */
    private final Map<String, Path> pluginJars = new ConcurrentHashMap<>();

    /** 插件信息数据访问接口，用来持久化插件记录到数据库 */
    private final PluginInfoRepository pluginInfoRepository;

    /**
     * 喵～ 构造注入！
     * Spring 会自动把 PluginInfoRepository 传进来～
     *
     * @param pluginInfoRepository 插件信息数据库访问接口
     */
    public PluginManager(PluginInfoRepository pluginInfoRepository) {
        this.pluginInfoRepository = pluginInfoRepository;
    }

    /**
     * 喵～ 初始化方法！
     * 应用启动时 @PostConstruct 会自动调用，
     * 创建插件目录并加载已有插件～
     */
    @PostConstruct
    public void init() {
        log.info("喵～ 插件系统正在初始化... (｀・ω・´)");
        try {
            // 确保插件目录存在
            Files.createDirectories(PLUGINS_DIR);
            log.info("喵～ 插件目录已就绪: {}", PLUGINS_DIR.toAbsolutePath());
            // 扫描并加载所有已有插件
            scanAndLoadPlugins();
        } catch (Exception e) {
            log.error("喵呜… 插件系统初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 喵～ 扫描 plugins/ 目录并加载所有 JAR 插件！
     * 遍历目录下所有 .jar 文件，逐个调用 loadPlugin 进行加载。
     */
    private void scanAndLoadPlugins() {
        log.info("喵～ 开始扫描插件目录...");

        if (!Files.exists(PLUGINS_DIR)) {
            log.warn("喵呜… 插件目录不存在: {}", PLUGINS_DIR.toAbsolutePath());
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(PLUGINS_DIR, "*.jar")) {
            for (Path jarPath : stream) {
                loadPlugin(jarPath);
            }
        } catch (Exception e) {
            log.error("喵呜… 扫描插件目录时出错: {}", e.getMessage(), e);
        }

        log.info("喵～ 插件扫描完成！当前已加载 {} 个插件", loadedPlugins.size());
    }

    /**
     * 喵～ 加载单个插件 JAR！
     * <p>
     * 加载流程：
     * <ol>
     *   <li>创建 PluginClassLoader 来加载 JAR 中的类</li>
     *   <li>读取 JAR 内的 plugin.yml 配置文件</li>
     *   <li>根据 plugin.yml 的 main 字段实例化插件主类</li>
     *   <li>调用 plugin.onLoad() 让插件做初始化</li>
     *   <li>注册插件提供的所有路由</li>
     *   <li>把插件信息保存到数据库</li>
     *   <li>记录插件到内存中以便管理</li>
     * </ol>
     *
     * @param jarPath 插件 JAR 文件的路径
     */
    public synchronized void loadPlugin(Path jarPath) {
        String jarName = jarPath.getFileName().toString();
        log.info("喵～ 正在加载插件 JAR: {}", jarName);

        // ===== 先从 JAR 中读取 plugin.yml 配置 =====
        String pluginId = null;
        String mainClass = null;
        String pluginName = null;
        String version = null;
        String author = null;
        String description = null;

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            // 查找 plugin.yml 文件（必须在 JAR 的根目录）
            JarEntry ymlEntry = jarFile.getJarEntry("plugin.yml");
            if (ymlEntry == null) {
                log.warn("喵呜… {} 里没有找到 plugin.yml，跳过加载", jarName);
                return;
            }

            // 用 SnakeYAML 解析 plugin.yml
            Yaml yaml = new Yaml();
            try (InputStream is = jarFile.getInputStream(ymlEntry)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) yaml.load(is);

                pluginId = (String) config.get("id");
                mainClass = (String) config.get("main");
                pluginName = (String) config.get("name");
                version = (String) config.get("version");
                author = (String) config.get("author");
                description = (String) config.get("description");
            }

            // 校验必填字段
            if (pluginId == null || pluginId.isBlank()) {
                log.warn("喵呜… {} 的 plugin.yml 缺少 id 字段", jarName);
                return;
            }
            if (mainClass == null || mainClass.isBlank()) {
                log.warn("喵呜… {} 的 plugin.yml 缺少 main 字段", jarName);
                return;
            }

        } catch (Exception e) {
            log.error("喵呜… 读取 {} 的 plugin.yml 失败: {}", jarName, e.getMessage(), e);
            return;
        }

        // ===== 检查插件是否已经加载过 =====
        if (loadedPlugins.containsKey(pluginId)) {
            log.info("喵～ 插件 {} 已经加载过了，跳过", pluginId);
            return;
        }

        // ===== 使用 ClassLoader 加载插件主类并实例化 =====
        PluginClassLoader loader = null;
        try {
            // 创建插件的独立 ClassLoader
            loader = new PluginClassLoader(jarPath);

            // 加载插件主类
            Class<?> mainClassObj = loader.loadClass(mainClass);

            // 实例化插件主类（必须有无参构造器）
            Plugin plugin = (Plugin) mainClassObj.getDeclaredConstructor().newInstance();

            // 如果 plugin.yml 中的 name/version 等字段为空，用插件类自身的返回值
            if (pluginName == null || pluginName.isBlank()) {
                pluginName = plugin.getName();
            }
            if (version == null || version.isBlank()) {
                version = plugin.getVersion();
            }
            if (author == null || author.isBlank()) {
                author = plugin.getAuthor();
            }
            if (description == null || description.isBlank()) {
                description = plugin.getDescription();
            }

            // ===== 调用插件的 onLoad 方法 =====
            try {
                plugin.onLoad();
                log.info("喵～ 插件 {} 的 onLoad 执行成功！", pluginId);
            } catch (Exception e) {
                log.error("喵呜… 插件 {} 的 onLoad 方法抛出异常: {}", pluginId, e.getMessage());
                // 即使 onLoad 失败，也继续注册，让插件有机会被卸载
            }

            // ===== 注册插件提供的路由 =====
            List<RouteDefinition> pluginRoutes = plugin.getRoutes();
            if (pluginRoutes != null) {
                for (RouteDefinition route : pluginRoutes) {
                    if (route.getPath() != null && route.getMethod() != null && route.getController() != null) {
                        routes.put(route.getPath(), route);
                        log.info("喵～ 注册路由: [{}] {}", route.getMethod().toUpperCase(), route.getPath());
                    } else {
                        log.warn("喵呜… 插件 {} 的路由定义不完整，跳过: {}/{}",
                            pluginId, route.getPath(), route.getMethod());
                    }
                }
            }

            // ===== 保存插件信息到数据库 =====
            savePluginInfo(pluginId, pluginName, version, author, description, true);

            // ===== 记录到内存缓存 =====
            loadedPlugins.put(pluginId, plugin);
            classLoaders.put(pluginId, loader);
            pluginJars.put(pluginId, jarPath);

            log.info("喵～ 插件 [{}] v{} 加载成功！作者: {} (ノ◕‿◕)ノ",
                pluginName, version, author);

        } catch (Exception e) {
            log.error("喵呜… 加载插件 {} 失败: {}", jarName, e.getMessage(), e);
            // 加载失败时关闭 ClassLoader
            if (loader != null) {
                try {
                    loader.close();
                } catch (Exception closeEx) {
                    log.warn("喵… 关闭插件 {} 的 ClassLoader 时出现异常: {}", jarName, closeEx.getMessage());
                }
            }
        }
    }

    /**
     * 喵～ 卸载指定 ID 的插件！
     * <p>
     * 卸载流程：
     * <ol>
     *   <li>调用 plugin.onUnload() 让插件做清理</li>
     *   <li>从路由表中移除该插件的所有路由</li>
     *   <li>关闭插件 ClassLoader 释放资源</li>
     *   <li>更新数据库状态为已禁用</li>
     *   <li>从内存缓存中移除</li>
     * </ol>
     *
     * @param pluginId 要卸载的插件唯一标识符
     */
    public synchronized void unloadPlugin(String pluginId) {
        log.info("喵～ 正在卸载插件: {}", pluginId);

        Plugin plugin = loadedPlugins.get(pluginId);
        if (plugin == null) {
            log.warn("喵呜… 插件 {} 未加载，无法卸载", pluginId);
            return;
        }

        // ===== 调用插件的 onUnload 方法 =====
        try {
            plugin.onUnload();
            log.info("喵～ 插件 {} 的 onUnload 执行成功！", pluginId);
        } catch (Exception e) {
            log.error("喵呜… 插件 {} 的 onUnload 方法抛出异常: {}", pluginId, e.getMessage());
        }

        // ===== 移除该插件注册的所有路由 =====
        List<RouteDefinition> pluginRoutes = plugin.getRoutes();
        if (pluginRoutes != null) {
            for (RouteDefinition route : pluginRoutes) {
                if (route.getPath() != null) {
                    routes.remove(route.getPath());
                    log.info("喵～ 移除路由: [{}] {}", route.getMethod(), route.getPath());
                }
            }
        }

        // ===== 关闭 ClassLoader 释放 JAR 文件句柄 =====
        PluginClassLoader loader = classLoaders.remove(pluginId);
        if (loader != null) {
            try {
                loader.close();
            } catch (Exception e) {
                log.warn("喵… 关闭插件 {} 的 ClassLoader 时出现异常: {}", pluginId, e.getMessage());
            }
        }

        // ===== 更新数据库状态 =====
        savePluginInfo(pluginId, plugin.getName(), plugin.getVersion(),
            plugin.getAuthor(), plugin.getDescription(), false);

        // ===== 从内存缓存中移除 =====
        loadedPlugins.remove(pluginId);
        pluginJars.remove(pluginId);

        log.info("喵～ 插件 [{}] 卸载完成！(｡•́︿•̀｡)", plugin.getName());
    }

    /**
     * 喵～ 获取所有已加载的插件列表！
     *
     * @return 已加载插件的列表（不可修改的视图）
     */
    public List<Plugin> getLoadedPlugins() {
        return List.copyOf(loadedPlugins.values());
    }

    /**
     * 喵～ 重新加载所有插件！
     * 先卸载当前所有已加载的插件，再重新扫描加载。
     */
    public synchronized void reloadAll() {
        log.info("喵～ 开始重新加载所有插件...");

        // 先卸载所有已加载的插件
        List<String> pluginIds = new ArrayList<>(loadedPlugins.keySet());
        for (String pluginId : pluginIds) {
            unloadPlugin(pluginId);
        }

        // 清空路由表确保干净
        routes.clear();

        // 重新扫描加载
        scanAndLoadPlugins();

        log.info("喵～ 所有插件重新加载完成！当前已加载 {} 个插件", loadedPlugins.size());
    }

    /**
     * 喵～ 根据路径查找对应的路由定义！
     * 请求分发控制器会调用这个方法来匹配路由。
     *
     * @param path 请求路径，比如 "/plugin/hello"
     * @return 匹配的路由定义，没找到返回 null
     */
    public RouteDefinition getRoute(String path) {
        return routes.get(path);
    }

    /**
     * 喵～ 获取当前已注册的所有路由表！
     * 这是一个不可修改的视图，防止外部意外修改路由表～
     *
     * @return 路由表的不可修改视图
     */
    public Map<String, RouteDefinition> getRoutes() {
        return Collections.unmodifiableMap(routes);
    }

    /**
     * 喵～ 定时扫描任务！
     * 每 5000ms（5 秒）扫描一次 plugins/ 目录，
     * 自动发现新添加的 JAR 文件并加载，
     * 同时检测已移除的 JAR 并卸载对应的插件。
     * <p>
     * 这样用户只需要往 plugins/ 目录里丢 JAR 文件，
     * 系统就会自动加载，完全不需要重启服务器喵～
     */
    @Scheduled(fixedRate = 5000)
    public void scanForChanges() {
        // 如果插件目录不存在，跳过本次扫描
        if (!Files.exists(PLUGINS_DIR)) {
            return;
        }

        try {
            // 记录当前所有 JAR 文件名
            Set<String> currentJars = new HashSet<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(PLUGINS_DIR, "*.jar")) {
                for (Path jarPath : stream) {
                    String jarName = jarPath.getFileName().toString();
                    currentJars.add(jarName);

                    // 检查这个 JAR 是否已经被加载
                    boolean isLoaded = false;
                    for (Path loadedPath : pluginJars.values()) {
                        if (loadedPath.getFileName().toString().equals(jarName)) {
                            isLoaded = true;
                            break;
                        }
                    }

                    // 新的 JAR 文件 → 自动加载
                    if (!isLoaded) {
                        log.info("喵～ 发现新插件 JAR: {}", jarName);
                        loadPlugin(jarPath);
                    }
                }
            }

            // 检查是否有 JAR 文件被删除 → 自动卸载对应插件
            List<String> toRemove = new ArrayList<>();
            for (Map.Entry<String, Path> entry : pluginJars.entrySet()) {
                String jarName = entry.getValue().getFileName().toString();
                if (!currentJars.contains(jarName)) {
                    toRemove.add(entry.getKey());
                }
            }
            for (String pluginId : toRemove) {
                log.info("喵～ 插件 JAR 已被移除，正在卸载插件: {}", pluginId);
                unloadPlugin(pluginId);
            }

        } catch (Exception e) {
            log.error("喵呜… 定时扫描插件目录时出错: {}", e.getMessage(), e);
        }
    }

    /**
     * 喵～ 把插件信息保存到数据库中！
     * 如果数据库中已有该插件的记录则更新，没有则新建。
     *
     * @param pluginId    插件唯一标识符
     * @param pluginName  插件名称
     * @param version     版本号
     * @param author      作者
     * @param description 描述
     * @param enabled     是否启用（true = 加载，false = 卸载）
     */
    private void savePluginInfo(String pluginId, String pluginName, String version,
                                 String author, String description, boolean enabled) {
        try {
            PluginInfo info = pluginInfoRepository.findByPluginId(pluginId)
                .orElse(new PluginInfo());

            info.setPluginId(pluginId);
            info.setPluginName(pluginName != null ? pluginName : pluginId);
            info.setVersion(version != null ? version : "0.0.0");
            info.setAuthor(author);
            info.setDescription(description);
            info.setEnabled(enabled);

            if (enabled) {
                info.setLoadedAt(LocalDateTime.now());
            }

            pluginInfoRepository.save(info);
            log.debug("喵～ 插件信息已保存到数据库: {} (启用={})", pluginId, enabled);
        } catch (Exception e) {
            log.error("喵呜… 保存插件 {} 信息到数据库时出错: {}", pluginId, e.getMessage());
        }
    }
}