package org.dam.component.status;

/**
 * 用户状态枚举
 * 穷举所有用户状态值，统一状态码与描述
 * 替代魔法值，避免在业务代码中直接使用 0/1/2/3 等数字字面量
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
public enum UserStatus {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用"),

    /**
     * 锁定
     */
    LOCKED(2, "锁定"),

    /**
     * 待审核
     */
    PENDING(3, "待审核");

    /**
     * 状态码（对应数据库 sys_user.status 字段）
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    UserStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据 code 反查枚举
     * 若 code 为空或未匹配，返回 null
     *
     * @param code 状态码
     * @return 对应的 UserStatus，无匹配返回 null
     */
    public static UserStatus ofCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 校验 code 是否为合法状态
     *
     * @param code 状态码
     * @return true 表示合法
     */
    public static boolean isValidCode(Integer code) {
        return ofCode(code) != null;
    }

}
