package org.dam.common.enums;

/**
 * 统一返回结果码枚举
 *
 * <h3>号段规划（业务码 ≠ HTTP 状态码）</h3>
 * <ul>
 *   <li>200        — 成功（兼容 RESTful 惯例，仅此一项与 HTTP 语义重合）</li>
 *   <li>1000~1999  — 业务通用异常段（鉴权/权限/参数/资源等）</li>
 *   <li>5000~5999  — 系统级异常段（框架/数据库/未知错误等）</li>
 *   <li>9999       — 通用兜底失败（仅用于 {@code Result.failed(String)} 等无明确语义场景）</li>
 * </ul>
 *
 * <p>注意：HTTP 响应状态码由 {@code @ResponseStatus} 或拦截器控制，与此处业务码是两个维度。
 *    前端一律以 {@code Result.code} 字段判断业务结果，不要依赖 HTTP status。</p>
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
public enum ResultCode {

    // ========================== 成功 ==========================
    /**
     * 操作成功（唯一与 HTTP 语义一致的业务码）
     */
    SUCCESS(200, "操作成功"),

    // ========================== 业务通用异常段 1xxx ==========================
    /**
     * 参数校验失败（入参格式、长度、必填等约束未通过）
     */
    PARAM_VALIDATE_FAILED(1400, "参数校验失败"),

    /**
     * 未登录或登录已过期（需重新认证获取 access_token）
     */
    UNAUTHORIZED(1401, "未授权"),

    /**
     * 已登录但无权限访问该资源（缺少角色或权限编码）
     */
    FORBIDDEN(1403, "无权限访问"),

    /**
     * 请求的资源不存在
     */
    NOT_FOUND(1404, "资源不存在"),

    /**
     * 通用业务异常（业务校验不通过、状态流转非法等，BizException 默认码）
     */
    BIZ_ERROR(1000, "业务异常"),

    // ========================== 系统级异常段 5xxx ==========================
    /**
     * 系统异常（未捕获的 RuntimeException、数据库错误等，兜底使用）
     */
    SYSTEM_ERROR(5000, "系统异常"),

    // ========================== 通用兜底 9999 ==========================
    /**
     * 通用失败（无更具体语义时使用，替代原 FAILED(500) 避免与 HTTP 状态码混淆）
     */
    COMMON_FAILED(9999, "操作失败");

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
