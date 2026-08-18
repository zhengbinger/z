package org.dam.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.dam.config.JwtProperties;
import org.dam.service.RefreshTokenService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Refresh Token 服务实现类
 * 基于 Redis 存储 refresh_token，使用 Lua 脚本保证轮转原子性，防止 replay attack
 * 单用户单 token 模型：新登录覆盖旧 token，等同踢人下线
 * 使用 StringRedisTemplate 避免 JSON 序列化包裹导致 Lua 脚本 ARGV 类型异常
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    /**
     * Redis Key 前缀：auth:refresh:{userId}
     */
    private static final String KEY_PREFIX = "auth:refresh:";

    /**
     * 轮转 refresh_token 的 Lua 脚本
     * 校验旧 token 与 Redis 中的一致后，CAS 替换为新 token，避免并发下的 replay attack
     * 返回 1 表示轮转成功，0 表示旧 token 已失效
     */
    private static final String ROTATE_LUA =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    redis.call('setex', KEYS[1], ARGV[3], ARGV[2]) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    private static final RedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>(ROTATE_LUA, Long.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtProperties jwtProperties;

    /**
     * 保存 refresh_token 到 Redis
     * TTL 与 refresh_token 自身过期时间一致，到期自动清理
     *
     * @param userId       用户 ID
     * @param refreshToken refresh_token 字符串
     */
    @Override
    public void save(Long userId, String refreshToken) {
        if (Objects.isNull(userId) || Objects.isNull(refreshToken)) {
            return;
        }
        String key = buildKey(userId);
        long ttlSeconds = jwtProperties.getRefreshTokenExpireDays() * 24L * 60L * 60L;
        stringRedisTemplate.opsForValue().set(key, refreshToken, ttlSeconds, TimeUnit.SECONDS);
        log.info("保存 refresh_token，userId={}，ttl={}秒", userId, ttlSeconds);
    }

    /**
     * 校验 refresh_token 是否在 Redis 中存在且与用户匹配
     * 用于刷新流程：必须 Redis 中存在且与请求的 token 一致才视为有效
     *
     * @param userId       用户 ID
     * @param refreshToken refresh_token 字符串
     * @return true 表示有效；false 表示已被吊销或被新登录覆盖
     */
    @Override
    public boolean validate(Long userId, String refreshToken) {
        if (Objects.isNull(userId) || Objects.isNull(refreshToken)) {
            return false;
        }
        String stored = stringRedisTemplate.opsForValue().get(buildKey(userId));
        return refreshToken.equals(stored);
    }

    /**
     * 轮转 refresh_token
     * 通过 Lua 脚本保证 CAS 原子性：只有 Redis 中当前 token 与旧 token 一致才替换
     * 防止攻击者用旧 token 在合法用户刷新后再次刷新
     *
     * @param userId          用户 ID
     * @param oldRefreshToken 旧的 refresh_token
     * @param newRefreshToken 新的 refresh_token
     * @return true 表示轮转成功；false 表示旧 token 已失效
     */
    @Override
    public boolean rotate(Long userId, String oldRefreshToken, String newRefreshToken) {
        if (Objects.isNull(userId) || Objects.isNull(oldRefreshToken) || Objects.isNull(newRefreshToken)) {
            return false;
        }
        long ttlSeconds = jwtProperties.getRefreshTokenExpireDays() * 24L * 60L * 60L;
        Long result = stringRedisTemplate.execute(
                ROTATE_SCRIPT,
                Collections.singletonList(buildKey(userId)),
                oldRefreshToken,
                newRefreshToken,
                String.valueOf(ttlSeconds)
        );
        boolean success = result != null && result == 1L;
        if (success) {
            log.info("轮转 refresh_token 成功，userId={}", userId);
        } else {
            log.warn("轮转 refresh_token 失败，旧 token 已失效，userId={}", userId);
        }
        return success;
    }

    /**
     * 吊销用户的 refresh_token
     * 登出、踢人下线、修改密码等场景调用
     *
     * @param userId 用户 ID
     */
    @Override
    public void revoke(Long userId) {
        if (Objects.isNull(userId)) {
            return;
        }
        Boolean deleted = stringRedisTemplate.delete(buildKey(userId));
        if (Boolean.TRUE.equals(deleted)) {
            log.info("吊销 refresh_token，userId={}", userId);
        }
    }

    /**
     * 构建 Redis Key
     *
     * @param userId 用户 ID
     * @return Redis Key 字符串
     */
    private String buildKey(Long userId) {
        return KEY_PREFIX + userId;
    }

}
