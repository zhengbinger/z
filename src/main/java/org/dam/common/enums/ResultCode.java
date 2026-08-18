package org.dam.common.enums;

/**
 * 统一返回结果码枚举
 * 包含通用业务码、成功码、错误码
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 通用失败
     */
    FAILED(500, "操作失败"),

    /**
     * 参数校验失败
     */
    PARAM_VALIDATE_FAILED(400, "参数校验失败"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权"),

    /**
     * 无权限访问
     */
    FORBIDDEN(403, "无权限访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 业务异常
     */
    BIZ_ERROR(1000, "业务异常"),

    /**
     * 系统异常
     */
    SYSTEM_ERROR(5000, "系统异常");

    private final Integer code;

    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
