-- =====================================================
-- 数据字典表结构 + 初始化数据
-- 设计文档：docs/dict-system-design.md
-- 包含：sys_dict_type（字典类型）+ sys_dict_data（字典项）
-- =====================================================

-- -----------------------------------------------------
-- 字典类型表 sys_dict_type
-- 一个 dict_code 对应一种分类（如 user_status）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
DROP TABLE IF EXISTS `sys_dict_type`;

CREATE TABLE `sys_dict_type` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `dict_code`    VARCHAR(50)  NOT NULL                COMMENT '字典编码（程序使用，唯一，如 user_status）',
    `dict_name_zh` VARCHAR(50)  NOT NULL                COMMENT '字典名称-中文（如：用户状态）',
    `dict_name_en` VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '字典名称-英文（如：User Status）',
    `status`       TINYINT      NOT NULL DEFAULT 1      COMMENT '状态（0-禁用，1-启用）',
    `remark`       VARCHAR(200)          DEFAULT NULL   COMMENT '备注',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`    VARCHAR(64)           DEFAULT NULL   COMMENT '创建人',
    `update_by`    VARCHAR(64)           DEFAULT NULL   COMMENT '更新人',
    `deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_code` (`dict_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '字典类型表';

-- -----------------------------------------------------
-- 字典项表 sys_dict_data
-- 一个 dict_value 对应一个选项（如 0-禁用）
-- -----------------------------------------------------
CREATE TABLE `sys_dict_data` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `dict_type_id`  BIGINT       NOT NULL                COMMENT '字典类型 ID',
    `dict_label_zh` VARCHAR(100) NOT NULL                COMMENT '字典标签-中文（展示用，如：禁用）',
    `dict_label_en` VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '字典标签-英文（如：Disabled）',
    `dict_value`    VARCHAR(100) NOT NULL                COMMENT '字典值（程序使用，如 0）',
    `css_class`     VARCHAR(100)          DEFAULT NULL   COMMENT '前端样式类（如 danger，便于按值着色）',
    `sort`          INT          NOT NULL DEFAULT 0     COMMENT '排序（数字越小越靠前）',
    `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态（0-禁用，1-启用）',
    `remark`        VARCHAR(200)          DEFAULT NULL   COMMENT '备注',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     VARCHAR(64)           DEFAULT NULL   COMMENT '创建人',
    `update_by`     VARCHAR(64)           DEFAULT NULL   COMMENT '更新人',
    `deleted`       TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_dict_type_id` (`dict_type_id`),
    KEY `idx_dict_value` (`dict_value`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '字典项表';

-- =====================================================
-- 字典类型初始化
-- =====================================================
INSERT INTO `sys_dict_type` (`dict_code`, `dict_name_zh`, `dict_name_en`, `remark`) VALUES
('user_status',     '用户状态',  'User Status',      'sys_user.status 字段对应值'),
('common_status',    '通用状态',  'Common Status',    'sys_role/sys_permission.status 共用'),
('permission_type',  '权限类型',  'Permission Type',  'sys_permission.type 字段对应值'),
('yes_no',           '是否',     'Yes/No',            '0-否，1-是'),
('auth_type',        '认证类型',  'Auth Type',         'sys_user_auth.auth_type 字段对应值');

-- =====================================================
-- 字典项初始化
-- =====================================================
-- user_status 用户状态
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_label_zh`, `dict_label_en`, `dict_value`, `css_class`, `sort`, `remark`) VALUES
((SELECT id FROM sys_dict_type WHERE dict_code = 'user_status'), '禁用',   'Disabled', '0', 'danger',  1, '用户被禁用，不可登录'),
((SELECT id FROM sys_dict_type WHERE dict_code = 'user_status'), '启用',   'Enabled', '1', 'success', 2, '正常状态'),
((SELECT id FROM sys_dict_type WHERE dict_code = 'user_status'), '锁定',   'Locked',  '2', 'warning', 3, '密码错误次数超限'),
((SELECT id FROM sys_dict_type WHERE dict_code = 'user_status'), '待审核', 'Pending', '3', 'info',    4, '注册后等待审核');

-- common_status 通用状态
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_label_zh`, `dict_label_en`, `dict_value`, `css_class`, `sort`) VALUES
((SELECT id FROM sys_dict_type WHERE dict_code = 'common_status'), '禁用', 'Disabled', '0', 'danger',  1),
((SELECT id FROM sys_dict_type WHERE dict_code = 'common_status'), '启用', 'Enabled', '1', 'success', 2);

-- permission_type 权限类型
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_label_zh`, `dict_label_en`, `dict_value`, `css_class`, `sort`) VALUES
((SELECT id FROM sys_dict_type WHERE dict_code = 'permission_type'), '菜单', 'Menu',   '1', 'primary', 1),
((SELECT id FROM sys_dict_type WHERE dict_code = 'permission_type'), '按钮', 'Button', '2', 'success', 2),
((SELECT id FROM sys_dict_type WHERE dict_code = 'permission_type'), '接口', 'API',    '3', 'warning', 3);

-- yes_no 是否
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_label_zh`, `dict_label_en`, `dict_value`, `css_class`, `sort`) VALUES
((SELECT id FROM sys_dict_type WHERE dict_code = 'yes_no'), '否', 'No',  '0', 'info',    1),
((SELECT id FROM sys_dict_type WHERE dict_code = 'yes_no'), '是', 'Yes', '1', 'success', 2);

-- auth_type 认证类型
INSERT INTO `sys_dict_data` (`dict_type_id`, `dict_label_zh`, `dict_label_en`, `dict_value`, `css_class`, `sort`) VALUES
((SELECT id FROM sys_dict_type WHERE dict_code = 'auth_type'), '密码',        'Password', 'password', 'primary', 1),
((SELECT id FROM sys_dict_type WHERE dict_code = 'auth_type'), '短信验证码',  'SMS Code',  'sms',      'success', 2),
((SELECT id FROM sys_dict_type WHERE dict_code = 'auth_type'), 'OAuth 第三方', 'OAuth',     'oauth',    'warning', 3);
