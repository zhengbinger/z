package org.dam.component.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录用户上下文
 * 通过 ThreadLocal 在当前请求线程内传递，承载当前操作者身份信息
 * 真实项目中由登录拦截器解析 Token 后填充，本项目示例由测试拦截器注入
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 是否已登录
     */
    private Boolean loggedIn;

}
