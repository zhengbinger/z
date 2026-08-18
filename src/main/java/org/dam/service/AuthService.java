package org.dam.service;

import org.dam.dto.AuthLoginDTO;
import org.dam.dto.RefreshTokenDTO;
import org.dam.vo.TokenVO;

/**
 * 认证服务接口
 * 定义用户登录、Token 刷新、登出等认证相关业务操作
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
public interface AuthService {

    /**
     * 用户登录
     * 校验账号密码通过后，签发 access_token + refresh_token
     * refresh_token 同步入 Redis 存储，用于后续刷新校验与吊销
     *
     * @param loginDTO 登录请求参数
     * @return Token 响应信息
     */
    TokenVO login(AuthLoginDTO loginDTO);

    /**
     * 刷新 Token
     * 用 refresh_token 换取新的 access_token + refresh_token
     * 校验链路：JWT 签名 → Redis 中存在且匹配 → 原子轮转存入新 token
     *
     * @param refreshDTO 刷新请求参数
     * @return 新的 Token 响应信息
     */
    TokenVO refresh(RefreshTokenDTO refreshDTO);

    /**
     * 用户登出
     * 吊销 Redis 中的 refresh_token，并清除该用户的权限缓存
     * access_token 为无状态 JWT，短期有效（直到自然过期），不引入黑名单
     *
     * @param userId 用户 ID
     */
    void logout(Long userId);

}
