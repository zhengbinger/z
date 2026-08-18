package org.dam.component.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * 安全上下文持有者
 * 基于 ThreadLocal 在当前请求线程内保存 {@link SecurityContext}
 * 请求结束时由拦截器统一调用 {@link #clear()} 清理，避免内存泄漏与线程复用串数据
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Component
public class SecurityContextHolder {

    private static final ThreadLocal<SecurityContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的安全上下文
     *
     * @param context 安全上下文
     */
    public static void set(SecurityContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取当前线程的安全上下文
     * 未设置返回 null
     *
     * @return 安全上下文
     */
    public static SecurityContext get() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 获取当前登录用户 ID
     * 未登录或未设置上下文返回 null
     *
     * @return 用户 ID
     */
    public static Long getCurrentUserId() {
        SecurityContext ctx = CONTEXT_HOLDER.get();
        return (ctx != null && Boolean.TRUE.equals(ctx.getLoggedIn())) ? ctx.getUserId() : null;
    }

    /**
     * 获取当前登录用户名
     * 未登录或未设置上下文返回 "anonymous"
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        SecurityContext ctx = CONTEXT_HOLDER.get();
        return (ctx != null && ctx.getUsername() != null) ? ctx.getUsername() : "anonymous";
    }

    /**
     * 是否已登录
     *
     * @return true 表示已登录
     */
    public static Boolean isLoggedIn() {
        SecurityContext ctx = CONTEXT_HOLDER.get();
        return ctx != null && Boolean.TRUE.equals(ctx.getLoggedIn());
    }

    /**
     * 清除当前线程的安全上下文
     * 必须在请求结束时调用，避免线程池复用导致的数据串用
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 容器销毁时打印日志，便于排查 ThreadLocal 未清理问题
     */
    @PreDestroy
    public void destroy() {
        log.info("SecurityContextHolder 销毁，ThreadLocal 状态检查");
        clear();
    }

}
