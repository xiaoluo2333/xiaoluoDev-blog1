package xl.xldevblog.main.repository;

import xl.xldevblog.main.entity.PluginInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 喵～ 插件信息数据访问接口！
 * <p>
 * 管理系统已加载插件的信息记录，
 * 可以在插件注册、卸载、状态查询时使用。
 * <p>
 * 插件系统可是很厉害的功能呢，要多来康康窝～ (๑╹ω╹๑)
 */
public interface PluginInfoRepository extends JpaRepository<PluginInfo, Long> {

    /**
     * 根据插件唯一标识符查找插件信息
     * <p>
     * pluginId 有 unique 约束，
     * 所以放心用 Optional 包装就好～
     * 比如查 "hello-world" 就能找到对应的插件记录喵！
     *
     * @param pluginId 插件唯一标识符（plugin.yml 里定义的）
     * @return 包含插件信息的 Optional，没找到就返回 Optional.empty()
     */
    Optional<PluginInfo> findByPluginId(String pluginId);
}