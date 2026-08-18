package org.dam.component.security;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dam.common.enums.ResultCode;
import org.dam.common.response.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Token 认证拦截器
 * 解析请求头 Authorization: Bearer <access_token>，校验后注入 {@link SecurityContextHolder}
 *
 * 处理策略：
 *   - 无 Token：匿名访问，loggedIn=false 放行（由 @RequiresLogin 注解决定是否拒绝）
 *   - Token 无效/过期：返回 401，拒绝请求
 *   - Token 类型非 access：返回 401（业务接口禁止使用 refresh_token）
 *   - Token 有效：注入用户身份上下文，放行
 *
 * 请求结束在 afterCompletion 清理 ThreadLocal，避免线程池复用串数据
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Component
public class TokenAuthInterceptor implements HandlerInterceptor {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String BEARER_PREFIX = "Bearer ";

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // 未携带 Authorization 头 → 匿名访问，由 @RequiresLogin 注解决定是否拦截
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            SecurityContext context = SecurityContext.builder()
                    .loggedIn(Boolean.FALSE)
                    .build();
            SecurityContextHolder.set(context);
            return true;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        // 校验 Token 签名 + 过期时间
        if (!jwtTokenUtil.verifyToken(token)) {
            log.warn("Token 校验失败，uri={}", request.getRequestURI());
            writeUnauthorized(response, "Token 无效或已过期，请重新登录");
            return false;
        }

        // 校验 Token 类型必须是 access（业务接口禁止使用 refresh_token）
        String tokenType = jwtTokenUtil.getTokenType(token);
        if (!JwtTokenUtil.TOKEN_TYPE_ACCESS.equals(tokenType)) {
            log.warn("Token 类型错误，业务接口需使用 access_token，tokenType={}", tokenType);
            writeUnauthorized(response, "请使用 access_token 访问业务接口");
            return false;
        }

        // 提取用户身份注入上下文
        Long userId = jwtTokenUtil.getUserId(token);
        String username = jwtTokenUtil.getUsername(token);
        SecurityContext context = SecurityContext.builder()
                .userId(userId)
                .username(username)
                .loggedIn(Boolean.TRUE)
                .build();
        SecurityContextHolder.set(context);
        log.debug("已注入安全上下文，userId={}，username={}", userId, username);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束清理 ThreadLocal，避免线程池复用串数据
        SecurityContextHolder.clear();
    }

    /**
     * 写入 401 未授权响应
     * HTTP 状态 401，body 为标准 Result 结构，便于前端统一处理认证失败
     *
     * @param response 响应对象
     * @param message  错误消息
     */
    private void writeUnauthorized(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            Result<Void> result = Result.failed(ResultCode.UNAUTHORIZED.getCode(), message);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            log.error("写入 401 响应失败", e);
        }
    }

}
