package org.dam.component.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录校验注解
 * 标注在 Controller 方法或类上，访问时必须已登录
 * 未登录抛出 401 Unauthorized
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresLogin {
}
