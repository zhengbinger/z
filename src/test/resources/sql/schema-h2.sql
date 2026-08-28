-- =====================================================
-- dam-server H2 兼容版 schema（MODE=MySQL）
-- 适配自 src/main/resources/sql/schema.sql / rbac_schema.sql / auth_schema.sql
-- 去除 H2 不支持的 MySQL 特性：
--   - ENGINE = InnoDB
--   - DEFAULT CHARSET = utf8mb4
--   - ON UPDATE CURRENT_TIMESTAMP
-- =====================================================

-- ========== 1. 用户表 sys_user ==========
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `username`    VARCHAR(30)  NOT NULL                COMMENT '用户名',
    `nickname`    VARCHAR(50)           DEFAULT NULL   COMMENT '昵称',
    `phone`       VARCHAR(20)           DEFAULT NULL   COMMENT '手机号',
    `email`       VARCHAR(100)          DEFAULT NULL   COMMENT '邮箱',
    `gender`      TINYINT      NOT NULL DEFAULT 0      COMMENT '性别',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态',
    `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   VARCHAR(64)           DEFAULT NULL   COMMENT '创建人',
    `update_by`   VARCHAR(64)           DEFAULT NULL   COMMENT '更新人',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    UNIQUE (`username`)
);

-- ========== 2. 角色表 sys_role ==========
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `role_code`   VARCHAR(50)  NOT NULL                COMMENT '角色编码',
    `role_name`   VARCHAR(50)  NOT NULL                COMMENT '角色名称',
    `description` VARCHAR(200)          DEFAULT NULL   COMMENT '角色描述',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态',
    `built_in`    TINYINT      NOT NULL DEFAULT 0      COMMENT '是否内置',
    `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   VARCHAR(64)          DEFAULT NULL   COMMENT '创建人',
    `update_by`   VARCHAR(64)          DEFAULT NULL   COMMENT '更新人',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    UNIQUE (`role_code`)
);

-- ========== 3. 权限表 sys_permission ==========
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `permission_code` VARCHAR(100) NOT NULL              COMMENT '权限编码',
    `permission_name` VARCHAR(50)  NOT NULL              COMMENT '权限名称',
    `type`           TINYINT      NOT NULL DEFAULT 3     COMMENT '类型',
    `parent_id`      BIGINT       NOT NULL DEFAULT 0     COMMENT '父级 ID',
    `path`           VARCHAR(200)          DEFAULT NULL  COMMENT '访问路径',
    `method`         VARCHAR(10)           DEFAULT NULL  COMMENT 'HTTP 方法',
    `description`    VARCHAR(200)          DEFAULT NULL  COMMENT '权限描述',
    `sort`           INT          NOT NULL DEFAULT 0     COMMENT '排序',
    `status`         TINYINT      NOT NULL DEFAULT 1      COMMENT '状态',
    `create_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`      VARCHAR(64)          DEFAULT NULL   COMMENT '创建人',
    `update_by`      VARCHAR(64)          DEFAULT NULL   COMMENT '更新人',
    `deleted`        TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    UNIQUE (`permission_code`)
);

-- ========== 4. 用户-角色关联表 sys_user_role ==========
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id`     BIGINT      NOT NULL                COMMENT '用户 ID',
    `role_id`     BIGINT      NOT NULL                COMMENT '角色 ID',
    `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`   VARCHAR(64)         DEFAULT NULL   COMMENT '创建人',
    `deleted`     TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    UNIQUE (`user_id`, `role_id`)
);

-- ========== 5. 角色-权限关联表 sys_role_permission ==========
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `role_id`       BIGINT      NOT NULL                COMMENT '角色 ID',
    `permission_id` BIGINT      NOT NULL                COMMENT '权限 ID',
    `create_time`   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`     VARCHAR(64)         DEFAULT NULL   COMMENT '创建人',
    `deleted`       TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    UNIQUE (`role_id`, `permission_id`)
);

-- ========== 6. 用户认证表 sys_user_auth ==========
DROP TABLE IF EXISTS `sys_user_auth`;
CREATE TABLE `sys_user_auth` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '关联用户 ID',
    `auth_type`   TINYINT      NOT NULL DEFAULT 1      COMMENT '认证类型',
    `identifier`  VARCHAR(100) NOT NULL                COMMENT '登录标识',
    `credential`  VARCHAR(200) NOT NULL                COMMENT '凭证',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态',
    `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   VARCHAR(64)           DEFAULT NULL   COMMENT '创建人',
    `update_by`   VARCHAR(64)           DEFAULT NULL   COMMENT '更新人',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    UNIQUE (`auth_type`, `identifier`)
);
