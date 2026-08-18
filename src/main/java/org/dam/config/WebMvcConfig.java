package org.dam.config;

import org.dam.component.security.TestSecurityInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Web MVC 配置
 * 注册安全拦截器，对所有接口请求注入安全上下文（基于 X-User-Id 请求头）
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private TestSecurityInterceptor testSecurityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(testSecurityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
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
