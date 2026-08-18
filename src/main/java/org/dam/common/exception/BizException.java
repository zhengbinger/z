package org.dam.common.exception;

import lombok.Getter;
import org.dam.common.enums.ResultCode;

/**
 * 业务异常
 * 用于业务流程中主动抛出的可预期异常
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 使用结果码构造业务异常
     *
     * @param resultCode 结果码枚举
     */
    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 使用结果码 + 自定义消息构造业务异常
     *
     * @param resultCode 结果码枚举
     * @param message    自定义消息
     */
    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    /**
     * 使用自定义码 + 消息构造业务异常
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用自定义消息构造业务异常（默认业务错误码）
     *
     * @param message 错误消息
     */
    public BizException(String message) {
        super(message);
        this.code = ResultCode.BIZ_ERROR.getCode();
    }

}
