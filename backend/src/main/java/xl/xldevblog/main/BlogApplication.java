package xl.xldevblog.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 喵～ 博客系统启动类！
 * 整个后端的入口就在这里啦，run 起来就能访问了 (｀・ω・´)
 *
 * @SpringBootApplication 包含了 @Configuration、@EnableAutoConfiguration、@ComponentScan
 * @EnableScheduling 开启定时任务，插件扫描就是靠这个
 */
@SpringBootApplication
@EnableScheduling
public class BlogApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        // 可以通过 --spring.config.additional-location=file:../config/blog.yml
        // 来加载外部配置覆盖 JAR 内部的 properties
        SpringApplication.run(BlogApplication.class, args);
    }
}