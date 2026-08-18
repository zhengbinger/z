package org.dam.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import lombok.extern.slf4j.Slf4j;
import org.dam.common.enums.ResultCode;
import org.dam.common.exception.BizException;
import org.dam.component.security.JwtTokenUtil;
import org.dam.component.status.UserStatus;
import org.dam.config.JwtProperties;
import org.dam.dto.AuthLoginDTO;
import org.dam.dto.RefreshTokenDTO;
import org.dam.entity.User;
import org.dam.entity.UserAuth;
import org.dam.mapper.UserAuthMapper;
import org.dam.mapper.UserMapper;
import org.dam.service.AuthService;
import org.dam.service.RefreshTokenService;
import org.dam.vo.TokenVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 认证服务实现类
 * 实现用户登录、Token 刷新、登出逻辑
 * 基于 BCrypt 密码校验 + JWT 双 Token + Redis 存储的 refresh_token
 * refresh_token 校验链路：JWT 签名 → Redis 中存在且匹配 → 原子轮转
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /**
     * 密码认证类型
     */
    private static final int AUTH_TYPE_PASSWORD = 1;

    @Resource
    private UserAuthMapper userAuthMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private RefreshTokenService refreshTokenService;

    /**
     * 用户登录
     * 校验账号密码通过后，签发 access_token + refresh_token
     * refresh_token 同步入 Redis 存储，用于后续刷新校验与吊销
     *
     * @param loginDTO 登录请求参数
     * @return Token 响应信息
     */
    @Override
    public TokenVO login(AuthLoginDTO loginDTO) {
        Integer authType = Objects.isNull(loginDTO.getAuthType()) ? AUTH_TYPE_PASSWORD : loginDTO.getAuthType();
        log.info("用户登录，identifier={}，authType={}", loginDTO.getIdentifier(), authType);

        // 1. 根据认证类型 + 登录标识查询认证记录
        UserAuth userAuth = userAuthMapper.selectByAuthTypeAndIdentifier(authType, loginDTO.getIdentifier());
        if (Objects.isNull(userAuth)) {
            log.warn("登录失败，认证记录不存在，identifier={}", loginDTO.getIdentifier());
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 2. BCrypt 校验密码（失败统一返回"用户名或密码错误"防止账号枚举）
        if (!BCrypt.checkpw(loginDTO.getCredential(), userAuth.getCredential())) {
            log.warn("登录失败，密码校验未通过，identifier={}", loginDTO.getIdentifier());
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 3. 查询用户主体，校验账号状态
        User user = userMapper.selectById(userAuth.getUserId());
        if (Objects.isNull(user)) {
            log.warn("登录失败，用户主体不存在，userId={}", userAuth.getUserId());
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        checkUserLoginable(user);

        // 4. 生成双 Token，refresh_token 同步 Redis 存储
        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), user.getUsername());
        refreshTokenService.save(user.getId(), refreshToken);
        log.info("用户登录成功，userId={}，username={}", user.getId(), user.getUsername());

        return buildTokenVO(accessToken, refreshToken, user.getId(), user.getUsername());
    }

    /**
     * 刷新 Token
     * 校验链路：JWT 签名 → Redis 中存在且匹配 → 原子轮转存入新 token
     * 通过 Lua 脚本保证轮转原子性，防止旧 refresh_token 被重复使用
     *
     * @param refreshDTO 刷新请求参数
     * @return 新的 Token 响应信息
     */
    @Override
    public TokenVO refresh(RefreshTokenDTO refreshDTO) {
        String refreshToken = refreshDTO.getRefreshToken();

        // 1. 校验 refresh_token 签名 + 过期
        if (!jwtTokenUtil.verifyToken(refreshToken)) {
            log.warn("Token 刷新失败，refresh_token 无效或已过期");
            throw new BizException(ResultCode.UNAUTHORIZED, "refresh_token 无效或已过期，请重新登录");
        }

        // 2. 校验 Token 类型必须是 refresh
        String tokenType = jwtTokenUtil.getTokenType(refreshToken);
        if (!JwtTokenUtil.TOKEN_TYPE_REFRESH.equals(tokenType)) {
            log.warn("Token 刷新失败，Token 类型错误，tokenType={}", tokenType);
            throw new BizException(ResultCode.UNAUTHORIZED, "Token 类型错误，请使用 refresh_token");
        }

        // 3. 取出用户信息，校验账号仍可登录
        Long userId = jwtTokenUtil.getUserId(refreshToken);
        String username = jwtTokenUtil.getUsername(refreshToken);
        User user = userMapper.selectById(userId);
        if (Objects.isNull(user)) {
            log.warn("Token 刷新失败，用户不存在，userId={}", userId);
            throw new BizException(ResultCode.UNAUTHORIZED, "用户不存在，请重新登录");
        }
        checkUserLoginable(user);

        // 4. 生成新双 Token，原子轮转 Redis 中的 refresh_token
        String newAccessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), user.getUsername());
        boolean rotated = refreshTokenService.rotate(userId, refreshToken, newRefreshToken);
        if (!rotated) {
            // Redis 中旧 token 已失效：可能已登出、被新登录覆盖、或被并发刷新抢先轮转
            log.warn("Token 刷新失败，refresh_token 在 Redis 中已失效，userId={}", userId);
            throw new BizException(ResultCode.UNAUTHORIZED, "refresh_token 已失效，请重新登录");
        }
        log.info("Token 刷新成功，userId={}，username={}", user.getId(), user.getUsername());

        return buildTokenVO(newAccessToken, newRefreshToken, user.getId(), user.getUsername());
    }

    /**
     * 用户登出
     * 吊销 Redis 中的 refresh_token，并精准清除该用户的权限/角色缓存
     * access_token 为无状态 JWT，短期有效（直到自然过期），不引入黑名单
     *
     * @param userId 用户 ID
     */
    @Override
    @CacheEvict(value = {"rbac:roles", "rbac:perms"}, key = "#userId")
    public void logout(Long userId) {
        if (Objects.isNull(userId)) {
            return;
        }
        refreshTokenService.revoke(userId);
        log.info("用户登出成功，userId={}", userId);
    }

    /**
     * 校验用户是否可登录
     * 仅启用状态可登录；禁用、锁定、待审核给出明确提示
     *
     * @param user 用户实体
     */
    private void checkUserLoginable(User user) {
        UserStatus status = UserStatus.ofCode(user.getStatus());
        if (status == UserStatus.ENABLED) {
            return;
        }
        if (status == UserStatus.DISABLED) {
            throw new BizException(ResultCode.BIZ_ERROR, "账号已禁用");
        }
        if (status == UserStatus.LOCKED) {
            throw new BizException(ResultCode.BIZ_ERROR, "账号已锁定");
        }
        if (status == UserStatus.PENDING) {
            throw new BizException(ResultCode.BIZ_ERROR, "账号待审核");
        }
        throw new BizException(ResultCode.BIZ_ERROR, "账号状态异常");
    }

    /**
     * 构建 Token 响应对象
     *
     * @param accessToken  access_token
     * @param refreshToken refresh_token
     * @param userId       用户 ID
     * @param username     用户名
     * @return Token 响应 VO
     */
    private TokenVO buildTokenVO(String accessToken, String refreshToken, Long userId, String username) {
        TokenVO vo = new TokenVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setTokenType("Bearer");
        vo.setExpiresIn(jwtProperties.getAccessTokenExpireMinutes() * 60L);
        vo.setUserId(userId);
        vo.setUsername(username);
        return vo;
    }

}
