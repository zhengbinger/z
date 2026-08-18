package org.dam.component.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 标注在 Controller 方法或类上，访问时必须拥有指定权限编码之一
 * 校验不通过抛出 403 Forbidden
 *
 * <p>用法示例：
 * <pre>
 * // 要求 user:add 权限
 * {@literal @}RequiresPermission("user:add")
 *
 * // 要求 user:add 或 user:update 权限（满足其一即可）
 * {@literal @}RequiresPermission({"user:add", "user:update"})
 *
 * // 逻辑与：要求同时具备 user:add 和 user:update 权限
 * {@literal @}RequiresPermission(value = {"user:add", "user:update"}, logical = Logical.AND)
 * </pre>
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /**
     * 权限编码数组
     */
    String[] value();

    /**
     * 多权限逻辑关系，默认 OR（满足其一即可）
     */
    Logical logical() default Logical.OR;

}
