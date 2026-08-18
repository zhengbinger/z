package org.dam.component.security;

import cn.hutool.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import org.dam.config.JwtProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 工具类
 * 基于 Hutool JWT 实现双 Token 机制：access_token + refresh_token
 * access_token：短时效，用于业务接口鉴权
 * refresh_token：长时效，仅用于换取新的 access_token
 * 通过 tokenType 区分两类 Token，防止混用
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Component
public class JwtTokenUtil {

    /**
     * access token 类型标识
     */
    public static final String TOKEN_TYPE_ACCESS = "access";

    /**
     * refresh token 类型标识
     */
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    @Resource
    private JwtProperties jwtProperties;

    /**
     * 生成 access_token
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return access_token 字符串
     */
    public String generateAccessToken(Long userId, String username) {
        long expireMillis = jwtProperties.getAccessTokenExpireMinutes() * 60L * 1000L;
        return generateToken(userId, username, TOKEN_TYPE_ACCESS, expireMillis);
    }

    /**
     * 生成 refresh_token
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return refresh_token 字符串
     */
    public String generateRefreshToken(Long userId, String username) {
        long expireMillis = jwtProperties.getRefreshTokenExpireDays() * 24L * 60L * 60L * 1000L;
        return generateToken(userId, username, TOKEN_TYPE_REFRESH, expireMillis);
    }

    /**
     * 校验 Token 签名 + 过期时间
     *
     * @param token Token 字符串
     * @return true 表示签名有效且未过期
     */
    public boolean verifyToken(String token) {
        try {
            JWT jwt = JWT.of(token).setKey(getKey());
            // verify 校验签名，validate 校验 nbf/exp 过期时间（leeway 0 秒）
            return jwt.verify() && jwt.validate(0);
        } catch (Exception e) {
            log.warn("Token 校验失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 Token 中解析用户 ID
     *
     * @param token Token 字符串
     * @return 用户 ID，解析失败返回 null
     */
    public Long getUserId(String token) {
        Object value = parseToken(token).getPayload("userId");
        if (value == null) {
            return null;
        }
        return Long.valueOf(value.toString());
    }

    /**
     * 从 Token 中解析用户名
     *
     * @param token Token 字符串
     * @return 用户名，解析失败返回 null
     */
    public String getUsername(String token) {
        Object value = parseToken(token).getPayload("username");
        return value == null ? null : value.toString();
    }

    /**
     * 从 Token 中解析 Token 类型（access / refresh）
     * 用于拦截器区分 Token 用途，避免业务接口用 refresh、刷新接口用 access
     *
     * @param token Token 字符串
     * @return Token 类型字符串，解析失败返回 null
     */
    public String getTokenType(String token) {
        Object value = parseToken(token).getPayload("tokenType");
        return value == null ? null : value.toString();
    }

    /**
     * 生成指定类型的 Token
     *
     * @param userId     用户 ID
     * @param username   用户名
     * @param tokenType  Token 类型（access / refresh）
     * @param expireMillis 过期毫秒数
     * @return Token 字符串
     */
    private String generateToken(Long userId, String username, String tokenType, long expireMillis) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expireMillis);
        return JWT.create()
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiresAt(expireAt)
                .setPayload("userId", userId)
                .setPayload("username", username)
                .setPayload("tokenType", tokenType)
                .setKey(getKey())
                .sign();
    }

    /**
     * 解析 Token 为 JWT 对象（已注入密钥）
     *
     * @param token Token 字符串
     * @return JWT 对象
     */
    private JWT parseToken(String token) {
        return JWT.of(token).setKey(getKey());
    }

    /**
     * 获取签名密钥字节数组
     *
     * @return 密钥字节数组
     */
    private byte[] getKey() {
        return jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
    }

}
