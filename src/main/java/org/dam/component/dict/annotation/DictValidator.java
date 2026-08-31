package org.dam.component.dict.annotation;

import org.dam.component.dict.DictValidatorImpl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字典校验注解
 * 标注在字段或参数上，校验值是否在指定 dictCode 的合法字典项范围内
 * 配合 Hibernate Validator 自动触发校验
 *
 * <p>用法示例：
 * <pre>
 * {@literal @}DictValidator(dictCode = "user_status", message = "用户状态值不合法")
 * private String status;
 * </pre>
 *
 * @author zhengbing
 * @since 2026-08-31
 **/
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DictValidatorImpl.class)
public @interface DictValidator {

    /**
     * 字典编码（如 user_status）
     *
     * @return 字典编码
     */
    String dictCode();

    /**
     * 校验失败消息
     *
     * @return 消息
     */
    String message() default "字典值不合法";

    /**
     * 校验分组
     *
     * @return 分组
     */
    Class<?>[] groups() default {};

    /**
     * 载荷
     *
     * @return 载荷
     */
    Class<? extends Payload>[] payload() default {};

}
