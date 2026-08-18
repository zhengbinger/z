package org.dam.service;

/**
 * Refresh Token 服务接口
 * 基于 Redis 存储 refresh_token，提供校验、轮转、吊销能力
 * 单用户单 token 模型：新登录覆盖旧 token，等同踢人下线
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
public interface RefreshTokenService {

    /**
     * 保存 refresh_token 到 Redis
     * TTL 与 refresh_token 自身过期时间一致，到期自动清理
     *
     * @param userId       用户 ID
     * @param refreshToken refresh_token 字符串
     */
    void save(Long userId, String refreshToken);

    /**
     * 校验 refresh_token 是否在 Redis 中存在且与用户匹配
     * 用于刷新流程：必须 Redis 中存在且与请求的 token 一致才视为有效
     *
     * @param userId       用户 ID
     * @param refreshToken refresh_token 字符串
     * @return true 表示有效；false 表示已被吊销或被新登录覆盖
     */
    boolean validate(Long userId, String refreshToken);

    /**
     * 轮转 refresh_token
     * 校验旧 token 有效后，删除旧 token 并存入新 token
     * 一次性原子操作，防止旧 token 被重复使用
     *
     * @param userId          用户 ID
     * @param oldRefreshToken 旧的 refresh_token
     * @param newRefreshToken 新的 refresh_token
     * @return true 表示轮转成功；false 表示旧 token 已失效
     */
    boolean rotate(Long userId, String oldRefreshToken, String newRefreshToken);

    /**
     * 吊销用户的 refresh_token
     * 登出、踢人下线、修改密码等场景调用
     *
     * @param userId 用户 ID
     */
    void revoke(Long userId);

}
