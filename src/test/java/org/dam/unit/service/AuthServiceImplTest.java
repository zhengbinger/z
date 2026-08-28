package org.dam.unit.service;

import cn.hutool.crypto.digest.BCrypt;
import org.dam.common.exception.BizException;
import org.dam.component.security.JwtTokenUtil;
import org.dam.config.JwtProperties;
import org.dam.dto.AuthLoginDTO;
import org.dam.dto.RefreshTokenDTO;
import org.dam.entity.User;
import org.dam.entity.UserAuth;
import org.dam.mapper.UserAuthMapper;
import org.dam.mapper.UserMapper;
import org.dam.service.RefreshTokenService;
import org.dam.service.impl.AuthServiceImpl;
import org.dam.support.TestDataBuilder;
import org.dam.vo.TokenVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.dam.support.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * AuthServiceImpl 单元测试
 * 覆盖 login/refresh/logout 全部分支
 * 纯 Mockito mock，无 Spring 上下文（@CacheEvict 不触发，由集成测试验证）
 * BCrypt.checkpw 用 mockStatic 隔离真实哈希计算
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
@DisplayName("功能: AuthService - 认证服务")
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserAuthMapper userAuthMapper;
    @Mock private UserMapper userMapper;
    @Mock private JwtTokenUtil jwtTokenUtil;
    @Mock private JwtProperties jwtProperties;
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    // =====================================================================
    // 场景组: 登录 login
    // =====================================================================
    @Nested
    @DisplayName("场景: 用户登录")
    class LoginScenario {

        @Test
        @DisplayName("假如 用户 admin 已注册且状态为启用 当 用户使用正确密码 admin123 登录 那么 返回 access_token 和 refresh_token 而且 Token 类型为 Bearer 而且 refresh_token 已保存到 Redis")
        void should_returnToken_when_loginWithCorrectPassword() {
            // given
            UserAuth auth = TestDataBuilder.userAuth().build();
            User user = TestDataBuilder.user().enabled().build();
            given(userAuthMapper.selectByAuthTypeAndIdentifier(AUTH_TYPE_PASSWORD, IDENTIFIER_ADMIN))
                .willReturn(auth);
            given(userMapper.selectById(ADMIN_ID)).willReturn(user);
            given(jwtTokenUtil.generateAccessToken(ADMIN_ID, IDENTIFIER_ADMIN))
                .willReturn("access-token");
            given(jwtTokenUtil.generateRefreshToken(ADMIN_ID, IDENTIFIER_ADMIN))
                .willReturn("refresh-token");
            given(jwtProperties.getAccessTokenExpireMinutes()).willReturn(ACCESS_TOKEN_EXPIRE_MINUTES);
            willDoNothing().given(refreshTokenService).save(eq(ADMIN_ID), anyString());

            try (MockedStatic<BCrypt> bcrypt = mockStatic(BCrypt.class)) {
                bcrypt.when(() -> BCrypt.checkpw(PASSWORD_ADMIN, HASH_ADMIN))
                      .thenReturn(true);

                // when
                AuthLoginDTO loginDTO = TestDataBuilder.loginDTO().build();
                TokenVO result = authService.login(loginDTO);

                // then
                assertThat(result)
                    .extracting("accessToken", "refreshToken", "tokenType", "userId", "username")
                    .containsExactly("access-token", "refresh-token", "Bearer", ADMIN_ID, IDENTIFIER_ADMIN);
                assertThat(result.getExpiresIn()).isEqualTo(ACCESS_TOKEN_EXPIRE_MINUTES * 60L);
                verify(refreshTokenService, times(1)).save(ADMIN_ID, "refresh-token");
            }
        }

        @Test
        @DisplayName("假如 用户 admin 的 authType 字段为 null 当 用户提交登录请求 那么 authType 默认为 1（密码认证）并按 1 查询认证记录")
        void should_usePasswordAuthType_when_authTypeIsNull() {
            // given
            AuthLoginDTO dto = TestDataBuilder.loginDTO().authType(null).build();
            UserAuth auth = TestDataBuilder.userAuth().build();
            User user = TestDataBuilder.user().build();
            given(userAuthMapper.selectByAuthTypeAndIdentifier(AUTH_TYPE_PASSWORD, IDENTIFIER_ADMIN))
                .willReturn(auth);
            given(userMapper.selectById(ADMIN_ID)).willReturn(user);
            given(jwtTokenUtil.generateAccessToken(anyLong(), anyString())).willReturn("at");
            given(jwtTokenUtil.generateRefreshToken(anyLong(), anyString())).willReturn("rt");
            given(jwtProperties.getAccessTokenExpireMinutes()).willReturn(ACCESS_TOKEN_EXPIRE_MINUTES);
            willDoNothing().given(refreshTokenService).save(anyLong(), anyString());

            try (MockedStatic<BCrypt> bcrypt = mockStatic(BCrypt.class)) {
                bcrypt.when(() -> BCrypt.checkpw(anyString(), anyString())).thenReturn(true);

                // when
                authService.login(dto);

                // then - 默认 authType 为 1
                verify(userAuthMapper).selectByAuthTypeAndIdentifier(1, IDENTIFIER_ADMIN);
            }
        }

        @Test
        @DisplayName("假如 用户 ghost 在认证表中不存在 当 用户使用任意密码登录 那么 返回\"用户名或密码错误\" 而且 不创建任何登录会话")
        void should_throwUserOrPasswordError_when_userAuthNotFound() {
            // given
            given(userAuthMapper.selectByAuthTypeAndIdentifier(AUTH_TYPE_PASSWORD, IDENTIFIER_GHOST))
                .willReturn(null);
            AuthLoginDTO dto = TestDataBuilder.loginDTO().identifier(IDENTIFIER_GHOST).build();

            // when + then
            assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining(ERROR_USER_OR_PASSWORD);
            verifyNoInteractions(refreshTokenService);
        }

        @Test
        @DisplayName("假如 用户 admin 已注册但密码错误 当 用户使用错误密码登录 那么 返回\"用户名或密码错误\" 而且 不查询用户主体 而且 不创建任何登录会话")
        void should_throwUserOrPasswordError_when_passwordNotMatch() {
            // given
            UserAuth auth = TestDataBuilder.userAuth().build();
            given(userAuthMapper.selectByAuthTypeAndIdentifier(AUTH_TYPE_PASSWORD, IDENTIFIER_ADMIN))
                .willReturn(auth);
            AuthLoginDTO dto = TestDataBuilder.loginDTO().credential(PASSWORD_WRONG).build();

            try (MockedStatic<BCrypt> bcrypt = mockStatic(BCrypt.class)) {
                bcrypt.when(() -> BCrypt.checkpw(PASSWORD_WRONG, HASH_ADMIN))
                      .thenReturn(false);

                // when + then
                assertThatThrownBy(() -> authService.login(dto))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining(ERROR_USER_OR_PASSWORD);
                verify(userMapper, never()).selectById(any());
                verifyNoInteractions(refreshTokenService);
            }
        }

        @Test
        @DisplayName("假如 用户认证记录存在但关联的用户主体不存在 当 密码校验通过后查询用户 那么 返回\"用户名或密码错误\" 而且 不创建任何登录会话")
        void should_throwUserOrPasswordError_when_userEntityNotFound() {
            // given
            UserAuth auth = TestDataBuilder.userAuth().build();
            given(userAuthMapper.selectByAuthTypeAndIdentifier(AUTH_TYPE_PASSWORD, IDENTIFIER_ADMIN))
                .willReturn(auth);
            given(userMapper.selectById(ADMIN_ID)).willReturn(null);
            AuthLoginDTO dto = TestDataBuilder.loginDTO().build();

            try (MockedStatic<BCrypt> bcrypt = mockStatic(BCrypt.class)) {
                bcrypt.when(() -> BCrypt.checkpw(anyString(), anyString())).thenReturn(true);

                // when + then
                assertThatThrownBy(() -> authService.login(dto))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining(ERROR_USER_OR_PASSWORD);
                verifyNoInteractions(refreshTokenService);
            }
        }

        @Test
        @DisplayName("假如 用户 admin 状态为禁用(0) 当 密码校验通过后 那么 返回\"账号已禁用\"")
        void should_throwAccountDisabled_when_userStatusDisabled() {
            // given
            UserAuth auth = TestDataBuilder.userAuth().build();
            User user = TestDataBuilder.user().disabled().build();
            given(userAuthMapper.selectByAuthTypeAndIdentifier(AUTH_TYPE_PASSWORD, IDENTIFIER_ADMIN))
                .willReturn(auth);
            given(userMapper.selectById(ADMIN_ID)).willReturn(user);

            try (MockedStatic<BCrypt> bcrypt = mockStatic(BCrypt.class)) {
                bcrypt.when(() -> BCrypt.checkpw(anyString(), anyString())).thenReturn(true);

                // when + then
                assertThatThrownBy(() -> authService.login(TestDataBuilder.loginDTO().build()))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining(ERROR_USER_DISABLED);
            }
        }

        @Test
        @DisplayName("用户锁定(2) 应返回\"账号已锁定\"")
        void should_throwAccountLocked_when_userStatusLocked() {
            // given
            UserAuth auth = TestDataBuilder.userAuth().build();
            User user = TestDataBuilder.user().locked().build();
            given(userAuthMapper.selectByAuthTypeAndIdentifier(AUTH_TYPE_PASSWORD, IDENTIFIER_ADMIN))
                .willReturn(auth);
            given(userMapper.selectById(ADMIN_ID)).willReturn(user);

            try (MockedStatic<BCrypt> bcrypt = mockStatic(BCrypt.class)) {
                bcrypt.when(() -> BCrypt.checkpw(anyString(), anyString())).thenReturn(true);

                // when + then
                assertThatThrownBy(() -> authService.login(TestDataBuilder.loginDTO().build()))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining(ERROR_USER_LOCKED);
            }
        }

        @Test
        @DisplayName("用户待审核(3) 应返回\"账号待审核\"")
        void should_throwAccountPending_when_userStatusPending() {
            // given
            UserAuth auth = TestDataBuilder.userAuth().build();
            User user = TestDataBuilder.user().pending().build();
            given(userAuthMapper.selectByAuthTypeAndIdentifier(AUTH_TYPE_PASSWORD, IDENTIFIER_ADMIN))
                .willReturn(auth);
            given(userMapper.selectById(ADMIN_ID)).willReturn(user);

            try (MockedStatic<BCrypt> bcrypt = mockStatic(BCrypt.class)) {
                bcrypt.when(() -> BCrypt.checkpw(anyString(), anyString())).thenReturn(true);

                // when + then
                assertThatThrownBy(() -> authService.login(TestDataBuilder.loginDTO().build()))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining(ERROR_USER_PENDING);
            }
        }
    }

    // =====================================================================
    // 场景组: 刷新 refresh
    // =====================================================================
    @Nested
    @DisplayName("场景: 刷新 Token")
    class RefreshScenario {

        @Test
        @DisplayName("假如 refresh_token 合法且未过期 而且 Token 类型为 refresh 而且 用户状态为启用 而且 Redis 中旧 token 与请求一致 当 调用刷新接口 那么 返回新的双 Token 而且 Redis 中旧 token 被原子轮转")
        void should_returnNewTokens_when_refreshTokenValid() {
            // given
            String oldRefresh = "valid-refresh-token";
            String newAccess = "new-access-token";
            String newRefresh = "new-refresh-token";

            given(jwtTokenUtil.verifyToken(oldRefresh)).willReturn(true);
            given(jwtTokenUtil.getTokenType(oldRefresh)).willReturn(TOKEN_TYPE_REFRESH);
            given(jwtTokenUtil.getUserId(oldRefresh)).willReturn(ADMIN_ID);
            given(jwtTokenUtil.getUsername(oldRefresh)).willReturn(IDENTIFIER_ADMIN);
            User user = TestDataBuilder.user().enabled().build();
            given(userMapper.selectById(ADMIN_ID)).willReturn(user);
            given(jwtTokenUtil.generateAccessToken(ADMIN_ID, IDENTIFIER_ADMIN)).willReturn(newAccess);
            given(jwtTokenUtil.generateRefreshToken(ADMIN_ID, IDENTIFIER_ADMIN)).willReturn(newRefresh);
            given(refreshTokenService.rotate(ADMIN_ID, oldRefresh, newRefresh)).willReturn(true);
            given(jwtProperties.getAccessTokenExpireMinutes()).willReturn(ACCESS_TOKEN_EXPIRE_MINUTES);

            RefreshTokenDTO dto = TestDataBuilder.refreshTokenDTO().refreshToken(oldRefresh).build();

            // when
            TokenVO result = authService.refresh(dto);

            // then
            assertThat(result)
                .extracting("accessToken", "refreshToken", "tokenType", "userId", "username")
                .containsExactly(newAccess, newRefresh, "Bearer", ADMIN_ID, IDENTIFIER_ADMIN);
            verify(refreshTokenService, times(1)).rotate(ADMIN_ID, oldRefresh, newRefresh);
        }

        @Test
        @DisplayName("refresh_token 签名或过期校验未通过应返回\"refresh_token 无效或已过期\"")
        void should_throwRefreshInvalid_when_verifyTokenFalse() {
            // given
            String oldRefresh = "expired-refresh-token";
            given(jwtTokenUtil.verifyToken(oldRefresh)).willReturn(false);
            RefreshTokenDTO dto = TestDataBuilder.refreshTokenDTO().refreshToken(oldRefresh).build();

            // when + then
            assertThatThrownBy(() -> authService.refresh(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining(ERROR_REFRESH_INVALID);
            verify(refreshTokenService, never()).rotate(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Token 类型错误（用 access_token 刷新）应返回\"Token 类型错误\"")
        void should_throwTokenTypeError_when_tokenTypeIsAccess() {
            // given
            String accessToken = "valid-but-access-type";
            given(jwtTokenUtil.verifyToken(accessToken)).willReturn(true);
            given(jwtTokenUtil.getTokenType(accessToken)).willReturn(TOKEN_TYPE_ACCESS);
            RefreshTokenDTO dto = TestDataBuilder.refreshTokenDTO().refreshToken(accessToken).build();

            // when + then
            assertThatThrownBy(() -> authService.refresh(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining(ERROR_TOKEN_TYPE);
            verify(refreshTokenService, never()).rotate(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("用户不存在应返回\"用户不存在\"")
        void should_throwUserNotFound_when_userEntityMissing() {
            // given
            String oldRefresh = "valid-refresh-token";
            given(jwtTokenUtil.verifyToken(oldRefresh)).willReturn(true);
            given(jwtTokenUtil.getTokenType(oldRefresh)).willReturn(TOKEN_TYPE_REFRESH);
            given(jwtTokenUtil.getUserId(oldRefresh)).willReturn(ADMIN_ID);
            given(jwtTokenUtil.getUsername(oldRefresh)).willReturn(IDENTIFIER_ADMIN);
            given(userMapper.selectById(ADMIN_ID)).willReturn(null);
            RefreshTokenDTO dto = TestDataBuilder.refreshTokenDTO().refreshToken(oldRefresh).build();

            // when + then
            assertThatThrownBy(() -> authService.refresh(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining(ERROR_USER_NOT_FOUND);
        }

        @Test
        @DisplayName("用户状态为禁用应返回\"账号已禁用\"")
        void should_throwAccountDisabled_when_userStatusChanged() {
            // given
            String oldRefresh = "valid-refresh-token";
            given(jwtTokenUtil.verifyToken(oldRefresh)).willReturn(true);
            given(jwtTokenUtil.getTokenType(oldRefresh)).willReturn(TOKEN_TYPE_REFRESH);
            given(jwtTokenUtil.getUserId(oldRefresh)).willReturn(ADMIN_ID);
            given(jwtTokenUtil.getUsername(oldRefresh)).willReturn(IDENTIFIER_ADMIN);
            User user = TestDataBuilder.user().disabled().build();
            given(userMapper.selectById(ADMIN_ID)).willReturn(user);
            RefreshTokenDTO dto = TestDataBuilder.refreshTokenDTO().refreshToken(oldRefresh).build();

            // when + then
            assertThatThrownBy(() -> authService.refresh(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining(ERROR_USER_DISABLED);
            verify(refreshTokenService, never()).rotate(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Redis 中旧 token 已失效（rotate 返回 false）应返回\"refresh_token 已失效\"")
        void should_throwRefreshExpired_when_rotateFailed() {
            // given
            String oldRefresh = "valid-refresh-token";
            String newRefresh = "new-refresh-token";
            given(jwtTokenUtil.verifyToken(oldRefresh)).willReturn(true);
            given(jwtTokenUtil.getTokenType(oldRefresh)).willReturn(TOKEN_TYPE_REFRESH);
            given(jwtTokenUtil.getUserId(oldRefresh)).willReturn(ADMIN_ID);
            given(jwtTokenUtil.getUsername(oldRefresh)).willReturn(IDENTIFIER_ADMIN);
            User user = TestDataBuilder.user().enabled().build();
            given(userMapper.selectById(ADMIN_ID)).willReturn(user);
            given(jwtTokenUtil.generateAccessToken(ADMIN_ID, IDENTIFIER_ADMIN)).willReturn("new-access");
            given(jwtTokenUtil.generateRefreshToken(ADMIN_ID, IDENTIFIER_ADMIN)).willReturn(newRefresh);
            given(refreshTokenService.rotate(ADMIN_ID, oldRefresh, newRefresh)).willReturn(false);
            RefreshTokenDTO dto = TestDataBuilder.refreshTokenDTO().refreshToken(oldRefresh).build();

            // when + then
            assertThatThrownBy(() -> authService.refresh(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining(ERROR_REFRESH_EXPIRED);
        }
    }

    // =====================================================================
    // 场景组: 登出 logout
    // =====================================================================
    @Nested
    @DisplayName("场景: 用户登出")
    class LogoutScenario {

        @Test
        @DisplayName("假如 用户 admin 已登录 当 用户调用登出 那么 Redis 中的 refresh_token 被吊销")
        void should_revokeRefreshToken_when_userIdNotNull() {
            // given
            willDoNothing().given(refreshTokenService).revoke(ADMIN_ID);

            // when
            authService.logout(ADMIN_ID);

            // then
            verify(refreshTokenService, times(1)).revoke(ADMIN_ID);
        }

        @Test
        @DisplayName("userId 为 null 时直接短路返回，不调用 revoke")
        void should_skipRevoke_when_userIdIsNull() {
            // when
            authService.logout(null);

            // then
            verify(refreshTokenService, never()).revoke(any());
        }
    }
}
