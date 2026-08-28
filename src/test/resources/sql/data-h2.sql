-- =====================================================
-- dam-server H2 兼容版测试数据
-- 覆盖：登录成功/失败、状态异常、RBAC 缓存命中各场景
-- =====================================================

-- ========== sys_user：覆盖各种状态 ==========
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `phone`, `email`, `gender`, `status`, `create_by`) VALUES
    (1, 'admin',       '管理员',   '13800138000', 'admin@dam.com',    1, 1, 'system'),
    (2, 'zhangsan',    '张三',     '13900139000', 'zhangsan@dam.com', 1, 1, 'system'),
    (3, 'lisi',        '李四',     '13700137000', 'lisi@dam.com',     2, 2, 'system'),
    (4, 'wangwu',      '王五',     '13600136000', 'wangwu@dam.com',   1, 0, 'system'),
    (5, 'pending_user', '待审核用户', '13500135000', 'pending@dam.com', 1, 3, 'system');

-- 重置 H2 自增起始值，避免与显式 ID 冲突
ALTER TABLE `sys_user` ALTER COLUMN `id` RESTART WITH 100;

-- ========== sys_role：内置两个角色 ==========
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `status`, `built_in`, `create_by`) VALUES
    (1, 'ADMIN', '超级管理员', '拥有系统全部权限', 1, 1, 'system'),
    (2, 'USER',  '普通用户',   '仅可查询用户列表', 1, 1, 'system');
ALTER TABLE `sys_role` ALTER COLUMN `id` RESTART WITH 100;

-- ========== sys_permission：覆盖用户/角色/权限模块 ==========
INSERT INTO `sys_permission` (`id`, `permission_code`, `permission_name`, `type`, `parent_id`, `path`, `method`, `description`, `sort`, `status`, `create_by`) VALUES
    (1,  'user:list',           '用户查询',   3, 0, '/user/page',           'POST',   '分页查询用户',   10, 1, 'system'),
    (2,  'user:get',            '用户详情',   3, 0, '/user/{id}',           'GET',    '查询用户详情',   11, 1, 'system'),
    (3,  'role:list',           '角色查询',   3, 0, '/role/page',           'POST',   '分页查询角色',   20, 1, 'system'),
    (4,  'permission:list',     '权限查询',   3, 0, '/permission/page',     'POST',   '分页查询权限',   30, 1, 'system');
ALTER TABLE `sys_permission` ALTER COLUMN `id` RESTART WITH 100;

-- ========== sys_user_role：admin→ADMIN, zhangsan→USER ==========
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_by`) VALUES
    (1, 1, 1, 'system'),
    (2, 2, 2, 'system');
ALTER TABLE `sys_user_role` ALTER COLUMN `id` RESTART WITH 100;

-- ========== sys_role_permission：ADMIN 拥有全部，USER 拥有 user:list/get ==========
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_by`) VALUES
    (1, 1, 1, 'system'),
    (2, 1, 2, 'system'),
    (3, 1, 3, 'system'),
    (4, 1, 4, 'system'),
    (5, 2, 1, 'system'),
    (6, 2, 2, 'system');
ALTER TABLE `sys_role_permission` ALTER COLUMN `id` RESTART WITH 100;

-- ========== sys_user_auth：admin 密码 admin123，zhangsan 密码 123456 ==========
-- BCrypt 哈希由 Hutool BCrypt.hashpw 生成，强度 10
INSERT INTO `sys_user_auth` (`id`, `user_id`, `auth_type`, `identifier`, `credential`, `status`, `create_by`) VALUES
    (1, 1, 1, 'admin',    '$2a$10$gj8Yv/sAHXp0v2dEyCAwduTI7DRGvBuNwYxTnBohlMeESq8RIvoAe', 1, 'system'),
    (2, 2, 1, 'zhangsan', '$2a$10$a6NO1Ub.nGjyzYehDQFlAeV.FzZMrXprCfdDlOUCJlmyUGHnHJqiW', 1, 'system');
ALTER TABLE `sys_user_auth` ALTER COLUMN `id` RESTART WITH 100;
