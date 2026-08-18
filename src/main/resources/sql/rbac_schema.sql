-- =====================================================
-- dam-server RBAC 角色权限体系表
-- 数据库：dam_db（默认）
-- 字符集：utf8mb4
-- 说明：基于 RBAC（Role-Based Access Control）模型
--       用户(sys_user) → 角色关联(sys_user_role) → 角色(sys_role)
--                        → 权限关联(sys_role_permission) → 权限(sys_permission)
-- =====================================================

USE `dam_db`;

-- =====================================================
-- 1. 角色表 sys_role
-- =====================================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `role_code`   VARCHAR(50)  NOT NULL                COMMENT '角色编码（程序使用，唯一）',
    `role_name`   VARCHAR(50)  NOT NULL                COMMENT '角色名称（展示用）',
    `description` VARCHAR(200)          DEFAULT NULL   COMMENT '角色描述',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态（0-禁用，1-启用）',
    `built_in`    TINYINT      NOT NULL DEFAULT 0      COMMENT '是否内置（0-否，1-是，内置角色不可删除）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   VARCHAR(64)          DEFAULT NULL   COMMENT '创建人',
    `update_by`   VARCHAR(64)          DEFAULT NULL   COMMENT '更新人',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';

-- =====================================================
-- 2. 权限表 sys_permission
--    type: 1-菜单，2-按钮，3-接口
-- =====================================================
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `permission_code` VARCHAR(100) NOT NULL              COMMENT '权限编码（程序使用，唯一，如 user:list）',
    `permission_name` VARCHAR(50)  NOT NULL              COMMENT '权限名称（展示用）',
    `type`           TINYINT      NOT NULL DEFAULT 3     COMMENT '类型（1-菜单，2-按钮，3-接口）',
    `parent_id`      BIGINT       NOT NULL DEFAULT 0     COMMENT '父级 ID（0-根节点）',
    `path`           VARCHAR(200)          DEFAULT NULL  COMMENT '访问路径',
    `method`         VARCHAR(10)           DEFAULT NULL  COMMENT 'HTTP 方法（GET/POST/PUT/DELETE）',
    `description`   VARCHAR(200)          DEFAULT NULL  COMMENT '权限描述',
    `sort`          INT          NOT NULL DEFAULT 0     COMMENT '排序（数字越小越靠前）',
    `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态（0-禁用，1-启用）',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     VARCHAR(64)          DEFAULT NULL   COMMENT '创建人',
    `update_by`     VARCHAR(64)          DEFAULT NULL   COMMENT '更新人',
    `deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '权限表';

-- =====================================================
-- 3. 用户-角色关联表 sys_user_role
-- =====================================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id`    BIGINT      NOT NULL                COMMENT '用户 ID',
    `role_id`    BIGINT      NOT NULL                COMMENT '角色 ID',
    `create_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`  VARCHAR(64)         DEFAULT NULL   COMMENT '创建人',
    `deleted`    TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户-角色关联表';

-- =====================================================
-- 4. 角色-权限关联表 sys_role_permission
-- =====================================================
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `role_id`       BIGINT      NOT NULL                COMMENT '角色 ID',
    `permission_id` BIGINT      NOT NULL                COMMENT '权限 ID',
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`    VARCHAR(64)         DEFAULT NULL   COMMENT '创建人',
    `deleted`      TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色-权限关联表';

-- =====================================================
-- 5. 初始化角色数据
-- =====================================================
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `status`, `built_in`)
VALUES ('ADMIN', '超级管理员', '拥有系统全部权限，不可删除', 1, 1),
       ('USER',  '普通用户',   '仅可查询用户列表，不可增删改', 1, 1);

-- =====================================================
-- 6. 初始化权限数据
-- =====================================================
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `type`, `parent_id`, `path`, `method`, `description`, `sort`, `status`)
VALUES
    -- 用户管理
    ('user:list',         '用户查询',   3, 0, '/user/page',         'POST',   '分页查询用户列表',   10, 1),
    ('user:get',          '用户详情',   3, 0, '/user/{id}',         'GET',    '查询用户详情',       11, 1),
    ('user:add',          '用户新增',   3, 0, '/user',              'POST',   '新增用户',           12, 1),
    ('user:update',       '用户修改',   3, 0, '/user',              'PUT',    '修改用户',           13, 1),
    ('user:delete',       '用户删除',   3, 0, '/user/{id}',         'DELETE', '删除用户',           14, 1),
    ('user:assignRole',   '分配角色',   3, 0, '/user/{id}/roles',   'PUT',    '给用户分配角色',     15, 1),
    -- 角色管理
    ('role:list',         '角色查询',   3, 0, '/role/page',         'POST',   '分页查询角色列表',   20, 1),
    ('role:get',          '角色详情',   3, 0, '/role/{id}',         'GET',    '查询角色详情',       21, 1),
    ('role:add',          '角色新增',   3, 0, '/role',             'POST',   '新增角色',           22, 1),
    ('role:update',       '角色修改',   3, 0, '/role',              'PUT',    '修改角色',           23, 1),
    ('role:delete',       '角色删除',   3, 0, '/role/{id}',         'DELETE', '删除角色',           24, 1),
    ('role:assignPermission', '角色分配权限', 3, 0, '/role/{id}/permissions', 'PUT', '给角色分配权限', 25, 1),
    -- 权限管理
    ('permission:list',   '权限查询',   3, 0, '/permission/page',   'POST',   '分页查询权限列表',   30, 1),
    ('permission:add',     '权限新增',   3, 0, '/permission',       'POST',   '新增权限',           31, 1),
    ('permission:update',  '权限修改',   3, 0, '/permission',       'PUT',    '修改权限',           32, 1),
    ('permission:delete',  '权限删除',   3, 0, '/permission/{id}',  'DELETE', '删除权限',           33, 1);

-- =====================================================
-- 7. 给 ADMIN 角色分配所有权限
-- =====================================================
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_by`)
SELECT 1, `id`, 'system' FROM `sys_permission`;

-- =====================================================
-- 8. 给 USER 角色分配 user:list 和 user:get 权限
-- =====================================================
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_by`)
VALUES
    (2, (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'user:list'), 'system'),
    (2, (SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'user:get'),  'system');

-- =====================================================
-- 9. 给用户分配角色
--    admin（user_id=1） → ADMIN 角色
--    zhangsan（user_id=2） → USER 角色
-- =====================================================
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `create_by`)
VALUES
    (1, 1, 'system'),
    (2, 2, 'system');
