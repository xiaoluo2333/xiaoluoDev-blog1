package xl.xldevblog.main.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import xl.xldevblog.main.entity.User;
import xl.xldevblog.main.repository.UserRepository;

/**
 * 喵～ 数据初始化器！
 * <p>
 * 应用启动的时候会自动跑起来，
 * 检查数据库里有没有管理员账号，
 * 没有的话就帮你创建一个默认的，
 * 省得还得手动去数据库里 insert 一条记录～
 * <p>
 * 第一次部署的时候特别有用哦！
 * 默认管理员账号：
 * <ul>
 *   <li>用户名: admin</li>
 *   <li>密码: admin（第一次部署后记得改密码喵！）</li>
 *   <li>邮箱: admin@blog.com</li>
 * </ul>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    /** 日志记录器，用来输出初始化信息 */
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /** 用户数据仓库，用来查询和保存用户 */
    private final UserRepository userRepository;

    /** Argon2id 密码编码器，用来哈希密码 */
    private final Argon2PasswordEncoder passwordEncoder;

    /**
     * 构造注入，Spring 会自动把 UserRepository 和 Argon2PasswordEncoder 传进来喵～
     *
     * @param userRepository  用户数据仓库
     * @param passwordEncoder Argon2id 密码编码器
     */
    public DataInitializer(UserRepository userRepository, Argon2PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 应用启动时的初始化逻辑
     * <p>
     * 流程：
     * <ol>
     *   <li>检查数据库里有没有管理员（isAdmin = true）</li>
     *   <li>如果有，输出日志并跳过初始化</li>
     *   <li>如果没有，创建默认管理员账号</li>
     *   <li>密码用 Argon2id 哈希后存入数据库</li>
     * </ol>
     *
     * @param args 命令行参数（咱用不上）
     */
    @Override
    public void run(String... args) {
        // 先看看数据库里有没有管理员
        if (userRepository.existsByIsAdminTrue()) {
            // 已经有管理员了，直接跳过初始化
            log.info("喵～ 数据库中已有管理员账号，跳过初始化 (｀・ω・´)");
            return;
        }

        // 没有管理员？那咱就来创建一个默认的吧！
        log.info("喵！检测到没有管理员账号，正在创建默认管理员...");

        // 创建管理员用户对象
        User admin = new User();
        admin.setUsername("admin");
        // 用 Argon2id 哈希密码，绝对不能存明文喵！
        admin.setPasswordHash(passwordEncoder.encode("admin"));
        admin.setEmail("admin@blog.com");
        admin.setDisplayName("管理员");
        admin.setIsAdmin(true);

        // 保存到数据库
        userRepository.save(admin);

        log.info("喵呜～ 默认管理员账号创建成功！(ノ◕‿◕)ノ");
        log.info("  用户名: admin");
        log.info("  密  码: admin（首次部署后请务必修改密码喵！）");
        log.info("  邮  箱: admin@blog.com");
        log.info("  显示名: 管理员");
    }
}