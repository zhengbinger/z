-- =====================================================================
-- 用户认证关联表 sys_user_auth
-- 设计原则：认证凭证与用户基本信息解耦，预留多认证方式扩展
--   - auth_type: 1-密码（当前实现），2-手机验证码，3-第三方 OAuth 等（预留）
--   - identifier: 登录标识（用户名/手机号/邮箱），用于登录时定位认证记录
--   - credential: 凭证（密码认证下为 BCrypt 哈希值）
-- 一个用户可拥有多条不同 auth_type 的认证记录，支持多种登录方式
-- =====================================================================

DROP TABLE IF EXISTS `sys_user_auth`;
CREATE TABLE `sys_user_auth` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '关联用户 ID（sys_user.id）',
    `auth_type`   TINYINT      NOT NULL DEFAULT 1      COMMENT '认证类型（1-密码，2-手机验证码，3-第三方 OAuth）',
    `identifier`  VARCHAR(100) NOT NULL                COMMENT '登录标识（用户名/手机号/邮箱）',
    `credential`  VARCHAR(200) NOT NULL                COMMENT '凭证（密码认证下为 BCrypt 哈希）',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态（0-禁用，1-启用）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   VARCHAR(64)           DEFAULT NULL   COMMENT '创建人',
    `update_by`   VARCHAR(64)           DEFAULT NULL   COMMENT '更新人',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_type_identifier` (`auth_type`, `identifier`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户认证关联表';

-- =====================================================================
-- 初始化认证数据（与 sys_user 现有用户对应）
--   admin    密码：admin123
--   zhangsan 密码：123456
-- BCrypt 哈希由 Hutool BCrypt.hashpw 生成，强度 10
-- =====================================================================
INSERT INTO `sys_user_auth` (`user_id`, `auth_type`, `identifier`, `credential`, `status`, `create_by`)
VALUES
    (1, 1, 'admin',    '$2a$10$gj8Yv/sAHXp0v2dEyCAwduTI7DRGvBuNwYxTnBohlMeESq8RIvoAe', 1, 'system'),
    (2, 1, 'zhangsan', '$2a$10$a6NO1Ub.nGjyzYehDQFlAeV.FzZMrXprCfdDlOUCJlmyUGHnHJqiW', 1, 'system');
