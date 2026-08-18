package org.dam.component.security.annotation;

/**
 * 多值校验逻辑枚举
 * 用于 {@link RequiresRole} 和 {@link RequiresPermission} 多值场景
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
public enum Logical {

    /**
     * 满足其一即可
     */
    OR,

    /**
     * 必须全部满足
     */
    AND,

}
