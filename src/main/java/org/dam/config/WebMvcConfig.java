package org.dam.config;

import org.dam.component.security.TokenAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Web MVC 配置
 * 注册 Token 认证拦截器，对所有请求解析 access_token 并注入安全上下文
 * 登录、刷新、文档接口放行，不走认证
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private TokenAuthInterceptor tokenAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/refresh",
                        "/doc.html",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error"
                );
    }

}
