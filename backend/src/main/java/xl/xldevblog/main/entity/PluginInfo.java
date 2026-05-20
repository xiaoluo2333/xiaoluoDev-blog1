package xl.xldevblog.main.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 喵～ 插件信息实体！
 * 记录已经加载的插件的信息，
 * 相当于插件系统的数据库档案。
 */
@Entity
@Table(name = "plugin_info")
public class PluginInfo {

    /** 插件记录 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 插件展示名称，比如 "HelloWorld Plugin" */
    @Column(nullable = false, length = 100)
    private String pluginName;

    /** 插件唯一标识符，比如 "hello-world"，在 plugin.yml 里定义的 */
    @Column(nullable = false, unique = true, length = 100)
    private String pluginId;

    /** 插件版本号 */
    @Column(nullable = false, length = 20)
    private String version;

    /** 插件作者 */
    @Column(length = 100)
    private String author;

    /** 插件描述 */
    @Column(length = 500)
    private String description;

    /** 插件是否启用 */
    @Column(nullable = false)
    private Boolean enabled = true;

    /** 插件加载时间 */
    private LocalDateTime loadedAt;

    @PrePersist
    protected void onLoad() {
        loadedAt = LocalDateTime.now();
    }

    // ===== Getter / Setter =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPluginName() { return pluginName; }
    public void setPluginName(String pluginName) { this.pluginName = pluginName; }

    public String getPluginId() { return pluginId; }
    public void setPluginId(String pluginId) { this.pluginId = pluginId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getLoadedAt() { return loadedAt; }
    public void setLoadedAt(LocalDateTime loadedAt) { this.loadedAt = loadedAt; }
}