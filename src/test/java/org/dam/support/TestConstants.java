package org.dam.support;

/**
 * 测试常量集中定义
 * 所有测试代码禁止使用魔法值，统一引用本类常量
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
public final class TestConstants {

    private TestConstants() {
    }

    // ========== 用户 ID ==========
    public static final Long ADMIN_ID = 1L;
    public static final Long USER_ID = 2L;
    public static final Long LOCKED_USER_ID = 3L;
    public static final Long DISABLED_USER_ID = 4L;
    public static final Long PENDING_USER_ID = 5L;

    // ========== 登录标识 ==========
    public static final String IDENTIFIER_ADMIN = "admin";
    public static final String IDENTIFIER_USER = "zhangsan";
    public static final String IDENTIFIER_GHOST = "ghost";

    // ========== 凭证（明文密码） ==========
    public static final String PASSWORD_ADMIN = "admin123";
    public static final String PASSWORD_USER = "123456";
    public static final String PASSWORD_WRONG = "wrong_password";

    // ========== BCrypt 哈希（强度 10） ==========
    public static final String HASH_ADMIN = "$2a$10$gj8Yv/sAHXp0v2dEyCAwduTI7DRGvBuNwYxTnBohlMeESq8RIvoAe";
    public static final String HASH_USER = "$2a$10$a6NO1Ub.nGjyzYehDQFlAeV.FzZMrXprCfdDlOUCJlmyUGHnHJqiW";

    // ========== 角色编码 ==========
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_UNKNOWN = "UNKNOWN";

    // ========== 权限编码 ==========
    public static final String PERM_USER_LIST = "user:list";
    public static final String PERM_USER_GET = "user:get";
    public static final String PERM_ROLE_LIST = "role:list";
    public static final String PERM_UNKNOWN = "unknown:perm";

    // ========== 认证类型 ==========
    public static final int AUTH_TYPE_PASSWORD = 1;

    // ========== Token 类型 ==========
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    // ========== 错误消息 ==========
    public static final String ERROR_USER_OR_PASSWORD = "用户名或密码错误";
    public static final String ERROR_USER_DISABLED = "账号已禁用";
    public static final String ERROR_USER_LOCKED = "账号已锁定";
    public static final String ERROR_USER_PENDING = "账号待审核";
    public static final String ERROR_REFRESH_INVALID = "refresh_token 无效或已过期，请重新登录";
    public static final String ERROR_TOKEN_TYPE = "Token 类型错误，请使用 refresh_token";
    public static final String ERROR_USER_NOT_FOUND = "用户不存在，请重新登录";
    public static final String ERROR_REFRESH_EXPIRED = "refresh_token 已失效，请重新登录";

    // ========== JWT 测试配置 ==========
    public static final String JWT_SECRET = "dam-test-secret-key-must-be-at-least-32-bytes-long";
    public static final String JWT_ISSUER = "dam-test";
    public static final long ACCESS_TOKEN_EXPIRE_MINUTES = 120L;
    public static final long REFRESH_TOKEN_EXPIRE_DAYS = 7L;
}
