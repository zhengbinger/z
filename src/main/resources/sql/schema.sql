-- =====================================================
-- dam-server 基础架构示例表
-- 数据库：dam_db（默认，可通过环境变量 DB_NAME 覆盖）
-- 字符集：utf8mb4
-- 说明：sys_user 表用于验证 Spring Boot + MyBatis Plus 架构的完整 CRUD 闭环
-- =====================================================

CREATE DATABASE IF NOT EXISTS `dam_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `dam_db`;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `username`    VARCHAR(30)  NOT NULL                COMMENT '用户名',
    `nickname`    VARCHAR(50)           DEFAULT NULL   COMMENT '昵称',
    `phone`       VARCHAR(20)           DEFAULT NULL   COMMENT '手机号',
    `email`       VARCHAR(100)          DEFAULT NULL   COMMENT '邮箱',
    `gender`      TINYINT      NOT NULL DEFAULT 0      COMMENT '性别（0-未知，1-男，2-女）',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态（0-禁用，1-启用）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   VARCHAR(64)           DEFAULT NULL   COMMENT '创建人',
    `update_by`   VARCHAR(64)           DEFAULT NULL   COMMENT '更新人',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户信息表';

-- =====================================================
-- 初始化示例数据
-- =====================================================
INSERT INTO `sys_user` (`username`, `nickname`, `phone`, `email`, `gender`, `status`)
VALUES ('admin', '管理员', '13800138000', 'admin@dam.com', 1, 1),
       ('zhangsan', '张三', '13900139000', 'zhangsan@dam.com', 1, 1),
       ('lisi', '李四', '13700137000', 'lisi@dam.com', 2, 1),
       ('wangwu', '王五', '13600136000', 'wangwu@dam.com', 1, 0);
