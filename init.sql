-- ============================================================
-- XiaoluoDev Blog - 数据库初始化脚本
-- 喵～ 这个脚本可以直接导入 MySQL 来创建数据库和表结构
-- 
-- 使用方法：
--   mysql -u root -p < init.sql
-- 或者在 MySQL 命令行中：
--   source d:/ai_code/awa_nya/init.sql
-- ============================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `xiaoluoDev_blog_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `xiaoluoDev_blog_db`;

-- ============================================================
-- 用户表（含普通用户和管理员，通过 is_admin 字段区分）
-- ============================================================
CREATE TABLE IF NOT EXISTS `users` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户唯一ID',
    `username`      VARCHAR(50)     NOT NULL                 COMMENT '用户名（唯一）',
    `password_hash` VARCHAR(255)    NOT NULL                 COMMENT 'Argon2id 哈希密码',
    `email`         VARCHAR(100)    DEFAULT NULL             COMMENT '邮箱',
    `display_name`  VARCHAR(100)    DEFAULT NULL             COMMENT '显示名称',
    `is_admin`      TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '是否管理员（0=普通用户, 1=管理员，系统唯一）',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表（含管理员）';

-- ============================================================
-- 文章表
-- ============================================================
CREATE TABLE IF NOT EXISTS `posts` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '文章唯一ID',
    `author_id`     BIGINT          DEFAULT NULL             COMMENT '作者ID（关联 users.id）',
    `title`         VARCHAR(255)    NOT NULL                 COMMENT '文章标题',
    `slug`          VARCHAR(255)    NOT NULL                 COMMENT 'URL 别名（唯一）',
    `content`       LONGTEXT        NOT NULL                 COMMENT 'Markdown 正文',
    `summary`       VARCHAR(500)    DEFAULT NULL             COMMENT '文章摘要',
    `tags`          VARCHAR(500)    DEFAULT NULL             COMMENT '标签（逗号分隔）',
    `status`        VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '状态（DRAFT=草稿, PUBLISHED=已发布）',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slug` (`slug`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_status_created` (`status`, `created_at` DESC),
    CONSTRAINT `fk_post_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客文章表';

-- ============================================================
-- 文件附件表（支持文件夹树结构）
-- ============================================================
CREATE TABLE IF NOT EXISTS `file_attachments` (
    `id`             BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '文件/文件夹唯一ID',
    `post_id`        BIGINT          DEFAULT NULL             COMMENT '关联文章ID（可为NULL，表示独立文件）',
    `file_path`      VARCHAR(1000)   NOT NULL                 COMMENT '磁盘上的实际路径（已防路径穿越校验）',
    `file_name`      VARCHAR(255)    NOT NULL                 COMMENT '显示给用户的文件名',
    `file_size`      BIGINT          DEFAULT NULL             COMMENT '文件大小（字节），文件夹为0',
    `content_type`   VARCHAR(100)    DEFAULT NULL             COMMENT 'MIME 类型',
    `is_folder`      TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '是否为文件夹（0=文件, 1=文件夹）',
    `parent_id`      BIGINT          DEFAULT NULL             COMMENT '父文件夹ID（NULL=根目录）',
    `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_parent_id` (`parent_id`),
    CONSTRAINT `fk_file_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_file_parent` FOREIGN KEY (`parent_id`) REFERENCES `file_attachments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件附件表（支持文件夹树）';

-- ============================================================
-- 插件信息表
-- ============================================================
CREATE TABLE IF NOT EXISTS `plugin_info` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '插件记录ID',
    `plugin_name`   VARCHAR(100)    NOT NULL                 COMMENT '插件展示名称',
    `plugin_id`     VARCHAR(100)    NOT NULL                 COMMENT '插件唯一标识符',
    `version`       VARCHAR(20)     NOT NULL                 COMMENT '插件版本号',
    `author`        VARCHAR(100)    DEFAULT NULL             COMMENT '插件作者',
    `description`   VARCHAR(500)    DEFAULT NULL             COMMENT '插件描述',
    `enabled`       TINYINT(1)      NOT NULL DEFAULT 1       COMMENT '是否启用（0=禁用, 1=启用）',
    `loaded_at`     DATETIME        DEFAULT NULL             COMMENT '插件加载时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_plugin_id` (`plugin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='插件信息表';

-- ============================================================
-- 默认管理员账号
-- 注意喵～ Argon2id 哈希每次生成都不同（因为有随机盐），
-- 所以这里无法预置哈希值。首次启动应用时 DataInitializer
-- 会自动创建默认管理员账号。
--
-- 如果你想手动创建，需要生成 Argon2id 哈希后再 INSERT。
-- 默认管理员：admin / admin
-- 首次部署请务必修改密码！
-- ============================================================
-- INSERT INTO `users` (`username`, `password_hash`, `email`, `display_name`, `is_admin`)
-- VALUES ('admin', '这里放Argon2id哈希值', 'admin@blog.com', '管理员', 1);

-- ============================================================
-- 完成喵～ 表结构已全部创建完毕 (｀・ω・´)
-- ============================================================