package xl.xldevblog.main.config;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 喵喵喵！Argon2id 密码编码器！
 * <p>
 * 用 Bouncy Castle 库实现的 Argon2id 哈希算法，
 * 这是目前最安全的密码哈希算法之一，
 * 能有效抵抗 GPU 和 ASIC 暴力破解喵！
 * <p>
 * 哈希的存储格式：
 * {@code $argon2id$v=19$m=65536,t=3,p=4$<Base64盐>$<Base64哈希>}
 * <p>
 * 参数说明（在安全性和性能之间取平衡）：
 * <ul>
 *   <li>m = 65536（64 MB 内存）</li>
 *   <li>t = 3（3 次迭代）</li>
 *   <li>p = 4（4 路并行）</li>
 * </ul>
 */
@Component
public class Argon2PasswordEncoder implements PasswordEncoder {

    /** 盐的长度（字节），16 字节 = 128 位，足够啦 */
    private static final int SALT_LENGTH = 16;

    /** 输出的哈希长度（字节），32 字节 = 256 位 */
    private static final int HASH_LENGTH = 32;

    /** Argon2id 迭代次数 */
    private static final int ITERATIONS = 3;

    /** 内存成本（KB），64 MB */
    private static final int MEMORY = 65536;

    /** 并行度 */
    private static final int PARALLELISM = 4;

    /** 安全的随机数生成器，用来生成盐 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 对明文密码进行 Argon2id 哈希
     *
     * @param rawPassword 明文密码
     * @return 编码后的密码字符串（包含盐和参数信息）
     */
    @Override
    public String encode(CharSequence rawPassword) {
        // 生成随机盐
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);

        // 配置 Argon2id 参数
        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEMORY)
                .withParallelism(PARALLELISM)
                .build();

        // 生成哈希
        byte[] hash = new byte[HASH_LENGTH];
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);
        generator.generateBytes(rawPassword.toString().toCharArray(), hash);

        // 格式：$argon2id$v=19$m=65536,t=3,p=4$base64盐$base64哈希
        return String.format(
                "$argon2id$v=19$m=%d,t=%d,p=%d$%s$%s",
                MEMORY,
                ITERATIONS,
                PARALLELISM,
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(hash)
        );
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 数据库中存储的编码后密码
     * @return true = 密码正确，false = 密码错误
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // 解析编码后的密码字符串
        String[] parts = encodedPassword.split("\\$");

        // 格式校验：应该有 6 段（开头空 + argon2id + v + 参数 + 盐 + 哈希）
        if (parts.length < 6) {
            return false;
        }

        // 算法校验：必须是 Argon2id
        if (!"argon2id".equals(parts[1])) {
            return false;
        }

        try {
            // 解码盐和哈希
            byte[] salt = Base64.getDecoder().decode(parts[4]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[5]);

            // 用同样的参数重新生成哈希
            Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                    .withSalt(salt)
                    .withIterations(ITERATIONS)
                    .withMemoryAsKB(MEMORY)
                    .withParallelism(PARALLELISM)
                    .build();

            byte[] actualHash = new byte[expectedHash.length];
            Argon2BytesGenerator generator = new Argon2BytesGenerator();
            generator.init(params);
            generator.generateBytes(rawPassword.toString().toCharArray(), actualHash);

            // 常量时间比较，防止时序攻击喵！
            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception e) {
            // 解析出错说明密码格式有问题，返回 false
            return false;
        }
    }

    /**
     * 常量时间比较，防止时序攻击（timing attack）
     * 普通比较会在第一个不同的字节就返回 false，
     * 攻击者可以通过响应时间推断出密码的相似程度。
     * 常量时间比较不管结果如何都遍历所有字节，安全喵！
     *
     * @param a 第一个字节数组
     * @param b 第二个字节数组
     * @return true = 两个数组内容相同
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}