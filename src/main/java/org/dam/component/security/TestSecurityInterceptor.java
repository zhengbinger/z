package org.dam.component.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 测试用安全拦截器
 * 通过请求头 X-User-Id 模拟当前登录用户身份，注入 {@link SecurityContextHolder}
 *
 * 生产环境应替换为基于 Token / Session 的真实鉴权拦截器
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Component
public class TestSecurityInterceptor implements HandlerInterceptor {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USERNAME);

        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdStr);
                SecurityContext context = SecurityContext.builder()
                        .userId(userId)
                        .username(username != null ? username : "user_" + userId)
                        .loggedIn(Boolean.TRUE)
                        .build();
                SecurityContextHolder.set(context);
                log.debug("已注入安全上下文，userId={}，username={}", userId, context.getUsername());
            } catch (NumberFormatException e) {
                log.warn("X-User-Id 格式非法，value={}", userIdStr);
            }
        } else {
            // 未携带 X-User-Id 视为匿名访问，loggedIn=false
            SecurityContext context = SecurityContext.builder()
                    .loggedIn(Boolean.FALSE)
                    .build();
            SecurityContextHolder.set(context);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束清理 ThreadLocal，避免线程池复用串数据
        SecurityContextHolder.clear();
    }

}
