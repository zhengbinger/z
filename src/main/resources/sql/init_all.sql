-- =====================================================================
-- BAESL 脚手架 · 全量数据库初始化脚本
-- 说明：按顺序执行 schema.sql（用户表）→ auth_schema.sql（认证表）→ rbac_schema.sql（角色权限体系）→ dict_schema.sql（数据字典）
-- 适用：首次建库、新环境初始化、CI/CD 自动化脚本
--
-- 使用方式 1（命令行 SOURCE 执行，需要当前目录为 sql/ 或使用绝对路径）：
--   mysql -uroot -p < init_all.sql
--
-- 使用方式 2（MySQL 客户端内）：
--   SOURCE /绝对路径/init_all.sql;
--
-- 注意：本脚本会 DROP 并重建所有表，所有已有数据将被清空！生产环境禁止执行！
-- =====================================================================

-- 第一步：建库 + 主用户表 sys_user + 初始用户数据
SOURCE schema.sql;

-- 第二步：用户认证关联表 sys_user_auth + 初始认证数据（admin/zhangsan 的 BCrypt 密码）
SOURCE auth_schema.sql;

-- 第三步：RBAC 体系四表（sys_role / sys_permission / sys_user_role / sys_role_permission）
--         + 内置角色（ADMIN/USER）+ 全部接口权限 + 初始用户角色分配
SOURCE rbac_schema.sql;

-- 第四步：数据字典两表（sys_dict_type / sys_dict_data）+ 5 个字典类型 / 14 个字典项初始数据
--         支撑前端下拉框、后端 @DictValidator 校验、枚举-字典一致性启动校验
SOURCE dict_schema.sql;
