package xl.xldevblog.main.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * 喵～ 插件类加载器！
 * <p>
 * 继承 URLClassLoader，专门用来加载插件 JAR 中的类。
 * 每个插件 JAR 都有一个独立的 ClassLoader 实例，
 * 这样可以实现插件之间的类隔离，避免冲突～
 * <p>
 * 加载策略：
 * <ul>
 *   <li>核心包（xl.xldevblog.main.plugin）下的类优先从父 ClassLoader 加载</li>
 *   <li>其他类优先从插件 JAR 中加载，找不到再委派给父 ClassLoader</li>
 * </ul>
 * 这样可以确保插件接口类在系统中是唯一的，不会出现类型转换异常喵！
 */
public class PluginClassLoader extends URLClassLoader {

    /** 核心白名单包名列表——这些包里的类必须从父 ClassLoader 加载 */
    private static final String[] CORE_PACKAGES = {
        "xl.xldevblog.main.plugin"
    };

    /** 父 ClassLoader，用来加载核心类和系统类 */
    private final ClassLoader parentClassLoader;

    /**
     * 喵～ 构造一个插件类加载器！
     *
     * @param jarPath 插件 JAR 文件的路径，比如 Paths.get("plugins/hello-world.jar")
     * @throws Exception 如果 JAR 路径无效或无法访问，会抛出异常喵
     */
    public PluginClassLoader(Path jarPath) throws Exception {
        // 把 JAR 路径转为 URL，并设置父 ClassLoader 为当前类的加载器
        super(new URL[]{jarPath.toUri().toURL()}, PluginClassLoader.class.getClassLoader());
        this.parentClassLoader = PluginClassLoader.class.getClassLoader();
    }

    /**
     * 喵～ 重写 loadClass 方法来实现自定义加载策略！
     * <p>
     * 加载顺序：
     * <ol>
     *   <li>如果是核心包（xl.xldevblog.main.plugin）中的类 → 直接从父 ClassLoader 加载</li>
     *   <li>否则先从本 ClassLoader 的 JAR 中查找（findClass）</li>
     *   <li>找不到再委派给父 ClassLoader</li>
     * </ol>
     * <p>
     * 这种策略的好处是：
     * 插件可以使用自己的依赖版本而不会和系统冲突，
     * 同时核心接口类在所有插件间共享，互相兼容～
     *
     * @param name    类的全限定名，比如 "xl.xldevblog.main.plugin.Plugin"
     * @param resolve 是否要解析这个类（一般传 true）
     * @return 加载到的 Class 对象
     * @throws ClassNotFoundException 如果找不到这个类就抛出喵
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 检查是否为核心包中的类
        for (String corePkg : CORE_PACKAGES) {
            if (name.startsWith(corePkg)) {
                // 核心类必须从父 ClassLoader 加载，确保系统内唯一性
                return super.loadClass(name, resolve);
            }
        }

        // 非核心类：先从插件 JAR 自己加载
        try {
            Class<?> clazz = findClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            // 插件 JAR 里没有这个类，回退到父 ClassLoader
            return super.loadClass(name, resolve);
        }
    }
}