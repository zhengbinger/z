package org.dam.component.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色校验注解
 * 标注在 Controller 方法或类上，访问时必须拥有指定角色之一
 * 校验不通过抛出 403 Forbidden
 *
 * <p>用法示例：
 * <pre>
 * // 要求 ADMIN 角色
 * {@literal @}RequiresRole("ADMIN")
 *
 * // 要求 ADMIN 或 MANAGER 角色（满足其一即可）
 * {@literal @}RequiresRole({"ADMIN", "MANAGER"})
 *
 * // 逻辑与：要求同时具备 ADMIN 和 MANAGER
 * {@literal @}RequiresRole(value = {"ADMIN", "MANAGER"}, logical = Logical.AND)
 * </pre>
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {

    /**
     * 角色编码数组
     */
    String[] value();

    /**
     * 多角色逻辑关系，默认 OR（满足其一即可）
     */
    Logical logical() default Logical.OR;

}
