package org.dam.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.dam.common.enums.ResultCode;

import java.io.Serializable;

/**
 * 统一返回结果封装
 * 用于规范 Controller 层的返回结构，包含状态码、消息、数据
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "统一返回结果")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    @Schema(description = "状态码")
    private Integer code;

    /**
     * 提示消息
     */
    @Schema(description = "提示消息")
    private String message;

    /**
     * 返回数据
     */
    @Schema(description = "返回数据")
    private T data;

    /**
     * 时间戳
     */
    @Schema(description = "响应时间戳")
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回（无数据）
     *
     * @param <T> 数据泛型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功返回（带数据）
     *
     * @param data 返回数据
     * @param <T>  数据泛型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（自定义消息 + 数据）
     *
     * @param message 提示消息
     * @param data    返回数据
     * @param <T>     数据泛型
     * @return 成功结果
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败返回（默认使用通用失败码 COMMON_FAILED）
     *
     * @param message 提示消息
     * @param <T>     数据泛型
     * @return 失败结果
     */
    public static <T> Result<T> failed(String message) {
        return failed(ResultCode.COMMON_FAILED.getCode(), message);
    }

    /**
     * 失败返回（指定结果码）
     *
     * @param resultCode 结果码枚举
     * @param <T>        数据泛型
     * @return 失败结果
     */
    public static <T> Result<T> failed(ResultCode resultCode) {
        return failed(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 失败返回（自定义码 + 消息）
     *
     * @param code    状态码
     * @param message 提示消息
     * @param <T>     数据泛型
     * @return 失败结果
     */
    public static <T> Result<T> failed(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 判断是否成功
     *
     * @return true 表示成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }

}
