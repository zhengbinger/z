package org.dam.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.dam.common.enums.ResultCode;
import org.dam.common.response.Result;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理 Controller 层抛出的各类异常，返回标准 Result 结构
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     *
     * @param e       业务异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e, HttpServletRequest request) {
        log.warn("业务异常，uri={}，code={}，message={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.failed(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常 - RequestBody 校验
     *
     * @param e       校验异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败，uri={}，message={}", request.getRequestURI(), message);
        return Result.failed(ResultCode.PARAM_VALIDATE_FAILED.getCode(), message);
    }

    /**
     * 参数校验异常 - 表单绑定
     *
     * @param e       绑定异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败，uri={}，message={}", request.getRequestURI(), message);
        return Result.failed(ResultCode.PARAM_VALIDATE_FAILED.getCode(), message);
    }

    /**
     * 参数校验异常 - 单参数校验
     *
     * @param e       约束违反异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数约束校验失败，uri={}，message={}", request.getRequestURI(), message);
        return Result.failed(ResultCode.PARAM_VALIDATE_FAILED.getCode(), message);
    }

    /**
     * 缺少请求参数
     *
     * @param e       参数缺失异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = "缺少必需参数：" + e.getParameterName();
        log.warn("缺少请求参数，uri={}，message={}", request.getRequestURI(), message);
        return Result.failed(ResultCode.PARAM_VALIDATE_FAILED.getCode(), message);
    }

    /**
     * 非法参数异常
     *
     * @param e       非法参数异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常，uri={}，message={}", request.getRequestURI(), e.getMessage());
        return Result.failed(ResultCode.PARAM_VALIDATE_FAILED.getCode(), e.getMessage());
    }

    /**
     * 未捕获异常兜底处理
     *
     * @param e       未知异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常，uri={}", request.getRequestURI(), e);
        return Result.failed(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMessage());
    }

}
