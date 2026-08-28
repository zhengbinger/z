package org.dam.web.controller;

import org.dam.common.exception.BizException;
import org.dam.common.enums.ResultCode;
import org.dam.component.security.SecurityContext;
import org.dam.component.security.SecurityContextHolder;
import org.dam.controller.AuthController;
import org.dam.dto.AuthLoginDTO;
import org.dam.dto.RefreshTokenDTO;
import org.dam.service.AuthService;
import org.dam.support.TestDataBuilder;
import org.dam.vo.TokenVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import static org.dam.support.TestConstants.IDENTIFIER_ADMIN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController Web 切片测试
 * 验证 HTTP 路由、@Valid 参数校验、Result 统一封装、BizException 全局转换
 * 不加载拦截器/AOP，仅 Controller + GlobalExceptionHandler 切片
 * /auth/logout 依赖 SecurityContextHolder（ThreadLocal），在 @BeforeEach 手动注入
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
@DisplayName("WebMvc: AuthController - 认证接口")
@Import(AuthController.class)
class AuthControllerTest extends BaseControllerMvcTest {

    @MockBean
    private AuthService authService;

    @AfterEach
    void tearDown() {
        // 清理 ThreadLocal，避免跨用例串数据
        SecurityContextHolder.clear();
    }

    // =====================================================================
    // 场景组: 登录 POST /auth/login
    // =====================================================================
    @Nested
    @DisplayName("场景: 用户登录")
    class Login {

        @Test
        @DisplayName("假如 用户 admin 提交正确凭证 当 POST /auth/login 那么 HTTP 200 而且 Result.code=200 而且 返回 access_token")
        void should_returnToken_when_loginSuccess() throws Exception {
            // given
            TokenVO tokenVO = TestDataBuilder.tokenVO()
                .accessToken("access-token-mock")
                .refreshToken("refresh-token-mock")
                .build();
            given(authService.login(any(AuthLoginDTO.class))).willReturn(tokenVO);

            // when + then
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        TestDataBuilder.loginDTO().build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.accessToken").value("access-token-mock"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-mock"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("假如 identifier 为空 当 POST /auth/login 那么 Result.code=1400 而且消息含\"登录标识不能为空\"")
        void should_returnValidateFailed_when_identifierBlank() throws Exception {
            // given
            AuthLoginDTO dto = new AuthLoginDTO();
            dto.setIdentifier("");
            dto.setCredential("any");

            // when + then
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("登录标识不能为空")));
        }

        @Test
        @DisplayName("假如 Service 抛 BizException 当 POST /auth/login 那么 Result.code 为业务码 而且消息透传")
        void should_returnBizError_when_serviceThrowsBizException() throws Exception {
            // given
            given(authService.login(any(AuthLoginDTO.class)))
                .willThrow(new BizException(1001, "用户名或密码错误"));

            // when + then
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        TestDataBuilder.loginDTO().build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
        }
    }

    // =====================================================================
    // 场景组: 刷新 POST /auth/refresh
    // =====================================================================
    @Nested
    @DisplayName("场景: 刷新 Token")
    class Refresh {

        @Test
        @DisplayName("假如 refresh_token 合法 当 POST /auth/refresh 那么 HTTP 200 而且返回新的双 Token")
        void should_returnNewTokens_when_refreshSuccess() throws Exception {
            // given
            TokenVO tokenVO = TestDataBuilder.tokenVO()
                .accessToken("new-access")
                .refreshToken("new-refresh")
                .build();
            given(authService.refresh(any(RefreshTokenDTO.class))).willReturn(tokenVO);

            // when + then
            RefreshTokenDTO dto = TestDataBuilder.refreshTokenDTO()
                .refreshToken("old-refresh-token").build();
            mockMvc.perform(post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.accessToken").value("new-access"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh"));
        }

        @Test
        @DisplayName("假如 refreshToken 为空 当 POST /auth/refresh 那么 Result.code=1400 而且消息含\"refreshToken 不能为空\"")
        void should_returnValidateFailed_when_refreshTokenBlank() throws Exception {
            // given
            RefreshTokenDTO dto = new RefreshTokenDTO();
            dto.setRefreshToken("");

            // when + then
            mockMvc.perform(post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("refreshToken 不能为空")));
        }
    }

    // =====================================================================
    // 场景组: 登出 POST /auth/logout
    // =====================================================================
    @Nested
    @DisplayName("场景: 用户登出")
    class Logout {

        @Test
        @DisplayName("假如 用户已登录 当 POST /auth/logout 那么 调用 authService.logout(userId) 而且 Result.data=true")
        void should_revokeToken_when_userLoggedIn() throws Exception {
            // given - 模拟拦截器注入的安全上下文
            SecurityContext context = SecurityContext.builder()
                .userId(1L)
                .username(IDENTIFIER_ADMIN)
                .loggedIn(Boolean.TRUE)
                .build();
            SecurityContextHolder.set(context);

            // when + then
            mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").value(true));

            verify(authService).logout(1L);
        }

        @Test
        @DisplayName("假如 未登录（无安全上下文） 当 POST /auth/logout 那么 userId=null 而且 authService.logout(null) 被调用")
        void should_passNullUserId_when_noSecurityContext() throws Exception {
            // given - 不设置 SecurityContext，模拟未登录状态

            // when + then - logout(null) 在 AuthServiceImpl 中短路返回，不抛异常
            mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()));

            verify(authService).logout(null);
        }
    }
}
