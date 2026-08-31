package org.dam.component.dict;

import cn.hutool.core.util.StrUtil;
import org.dam.component.dict.annotation.DictValidator;
import org.dam.service.DictService;

import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 字典校验实现
 * 由 Hibernate Validator 调用，校验字段值是否在 dictCode 对应字典的合法范围内
 * 泛型用 Object，支持 String / Integer / Long 等任意类型字段
 * 空值与空字符串不校验（由 @NotBlank / @NotNull 负责）
 * 依赖 Spring Boot validation starter 的 SpringConstraintValidatorFactory 注入 DictService
 *
 * @author zhengbing
 * @since 2026-08-31
 **/
public class DictValidatorImpl implements ConstraintValidator<DictValidator, Object> {

    @Resource
    private DictService dictService;

    private String dictCode;

    @Override
    public void initialize(DictValidator annotation) {
        this.dictCode = annotation.dictCode();
    }

    /**
     * 校验值是否合法
     * null 与空白字符串放行（由其他注解负责）
     * 其他值统一 String.valueOf 后校验，兼容 String "0" 与 Integer 0
     *
     * @param value   待校验值
     * @param context 校验上下文
     * @return true 表示合法
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence && StrUtil.isBlank((CharSequence) value)) {
            return true;
        }
        if (dictService == null) {
            return true;
        }
        return dictService.isValidValue(dictCode, String.valueOf(value));
    }

}
