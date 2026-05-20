package xl.xldevblog.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xl.xldevblog.main.entity.User;

import java.util.Optional;

/**
 * 喵～ 用户数据仓库！
 * Spring Data JPA 自动实现 CRUD，
 * 咱只需要定义查询方法就行啦！
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * <p>
     * 登录的时候根据用户名查用户信息，
     * 注册的时候也要检查用户名是否已存在喵～
     *
     * @param username 用户名
     * @return 包含用户的 Optional，没找到就返回 Optional.empty()
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * <p>
     * 注册的时候检查邮箱是否已经被注册了喵～
     *
     * @param email 用户邮箱
     * @return 包含用户的 Optional，没找到就返回 Optional.empty()
     */
    Optional<User> findByEmail(String email);

    /**
     * 判断用户名是否已被注册
     *
     * @param username 用户名
     * @return true = 已存在，false = 不存在
     */
    boolean existsByUsername(String username);

    /**
     * 判断邮箱是否已被注册
     *
     * @param email 用户邮箱
     * @return true = 已存在，false = 不存在
     */
    boolean existsByEmail(String email);

    /**
     * 查找管理员用户（isAdmin = true）
     * 由于整个系统只有一个管理员，
     * 所以返回 Optional<User> 就够啦
     *
     * @return 管理员的 Optional，没有管理员的话就是空的
     */
    Optional<User> findByIsAdminTrue();

    /**
     * 判断是否存在管理员
     *
     * @return true = 已经有管理员了，false = 还没有
     */
    boolean existsByIsAdminTrue();
}