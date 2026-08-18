package org.dam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 认证配置属性
 * 绑定 application.yml 中 jwt.* 配置项
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 签名密钥（HS256 建议 >= 32 字节）
     */
    private String secret;

    /**
     * 签发者
     */
    private String issuer;

    /**
     * access_token 过期时间（分钟），默认 120 分钟
     */
    private long accessTokenExpireMinutes = 120L;

    /**
     * refresh_token 过期时间（天），默认 7 天
     */
    private long refreshTokenExpireDays = 7L;

}
