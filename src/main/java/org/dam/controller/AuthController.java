package org.dam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.dam.common.response.Result;
import org.dam.component.security.SecurityContextHolder;
import org.dam.component.security.annotation.RequiresLogin;
import org.dam.dto.AuthLoginDTO;
import org.dam.dto.RefreshTokenDTO;
import org.dam.service.AuthService;
import org.dam.vo.TokenVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 认证管理 Controller
 * 提供用户登录、Token 刷新、登出接口（登录/刷新在 WebMvcConfig 中放行，登出需登录）
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户登录、Token 刷新、登出接口")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 用户登录
     * 校验账号密码通过后，签发 access_token + refresh_token
     * refresh_token 同步入 Redis 存储，用于后续刷新校验与吊销
     *
     * @param loginDTO 登录请求参数
     * @return Token 响应信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "账号密码校验通过后返回双 Token，access 用于业务接口，refresh 用于无感续期")
    public Result<TokenVO> login(@Valid @RequestBody AuthLoginDTO loginDTO) {
        TokenVO vo = authService.login(loginDTO);
        return Result.success(vo);
    }

    /**
     * 刷新 Token
     * 用 refresh_token 换取新的 access_token + refresh_token
     * 校验链路：JWT 签名 → Redis 中存在且匹配 → 原子轮转存入新 token
     *
     * @param refreshDTO 刷新请求参数
     * @return 新的 Token 响应信息
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "access_token 过期后，前端用 refresh_token 调本接口无感换取新 Token")
    public Result<TokenVO> refresh(@Valid @RequestBody RefreshTokenDTO refreshDTO) {
        TokenVO vo = authService.refresh(refreshDTO);
        return Result.success(vo);
    }

    /**
     * 用户登出
     * 吊销 Redis 中的 refresh_token，并精准清除该用户的权限/角色缓存
     * access_token 为无状态 JWT，短期有效（直到自然过期），不引入黑名单
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    @RequiresLogin
    @Operation(summary = "用户登出", description = "吊销 refresh_token 并清除权限缓存，access_token 自然过期失效")
    public Result<Boolean> logout() {
        Long userId = SecurityContextHolder.getCurrentUserId();
        authService.logout(userId);
        return Result.success(Boolean.TRUE);
    }

}
