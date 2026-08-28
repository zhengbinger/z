package org.dam.unit.component;

import org.dam.component.security.JwtTokenUtil;
import org.dam.config.JwtProperties;
import org.dam.support.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dam.support.TestConstants.*;

/**
 * JwtTokenUtil 纯单元测试
 * 纯函数测试，无 Spring 上下文，无外部依赖
 * 验证 access/refresh Token 类型区分、过期校验、签名篡改
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
@DisplayName("功能: JwtTokenUtil - Token 生成与校验")
class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(JWT_SECRET);
        jwtProperties.setIssuer(JWT_ISSUER);
        jwtProperties.setAccessTokenExpireMinutes(ACCESS_TOKEN_EXPIRE_MINUTES);
        jwtProperties.setRefreshTokenExpireDays(REFRESH_TOKEN_EXPIRE_DAYS);

        jwtTokenUtil = new JwtTokenUtil();
        // JwtProperties 字段是 @Resource private 注入，用反射设置
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtProperties", jwtProperties);
    }

    @Nested
    @DisplayName("场景: 生成 Token")
    class GenerateToken {

        @Test
        @DisplayName("假如 用户 admin 已注册且状态为启用 当 系统生成 access_token 那么 access_token 不为空 而且 Token 类型为 access")
        void should_returnAccessTokenWithTypeAccess_when_generateAccessToken() {
            // when
            String token = jwtTokenUtil.generateAccessToken(ADMIN_ID, IDENTIFIER_ADMIN);

            // then
            assertThat(token).isNotBlank();
            assertThat(jwtTokenUtil.getTokenType(token)).isEqualTo(TOKEN_TYPE_ACCESS);
        }

        @Test
        @DisplayName("生成 refresh_token 时类型应为 refresh")
        void should_returnRefreshTokenWithTypeRefresh_when_generateRefreshToken() {
            // when
            String token = jwtTokenUtil.generateRefreshToken(ADMIN_ID, IDENTIFIER_ADMIN);

            // then
            assertThat(token).isNotBlank();
            assertThat(jwtTokenUtil.getTokenType(token)).isEqualTo(TOKEN_TYPE_REFRESH);
        }

        @Test
        @DisplayName("access_token 与 refresh_token 应不相等（即使同一用户）")
        void should_notEqual_when_compareAccessAndRefreshToken() {
            // when
            String access = jwtTokenUtil.generateAccessToken(ADMIN_ID, IDENTIFIER_ADMIN);
            String refresh = jwtTokenUtil.generateRefreshToken(ADMIN_ID, IDENTIFIER_ADMIN);

            // then
            assertThat(access).isNotEqualTo(refresh);
        }
    }

    @Nested
    @DisplayName("场景: 解析 Token 载荷")
    class ParseToken {

        @Test
        @DisplayName("解析 access_token 应返回正确的 userId 和 username")
        void should_returnUserIdAndUsername_when_parseAccessToken() {
            // given
            String token = jwtTokenUtil.generateAccessToken(ADMIN_ID, IDENTIFIER_ADMIN);

            // when + then
            assertThat(jwtTokenUtil.getUserId(token)).isEqualTo(ADMIN_ID);
            assertThat(jwtTokenUtil.getUsername(token)).isEqualTo(IDENTIFIER_ADMIN);
        }

        @Test
        @DisplayName("解析 refresh_token 也应返回正确的用户信息")
        void should_returnUserIdAndUsername_when_parseRefreshToken() {
            // given
            String token = jwtTokenUtil.generateRefreshToken(USER_ID, IDENTIFIER_USER);

            // when + then
            assertThat(jwtTokenUtil.getUserId(token)).isEqualTo(USER_ID);
            assertThat(jwtTokenUtil.getUsername(token)).isEqualTo(IDENTIFIER_USER);
        }
    }

    @Nested
    @DisplayName("场景: 校验 Token 签名与过期")
    class VerifyToken {

        @Test
        @DisplayName("合法且未过期的 Token 应校验通过")
        void should_returnTrue_when_tokenValidAndNotExpired() {
            // given
            String token = jwtTokenUtil.generateAccessToken(ADMIN_ID, IDENTIFIER_ADMIN);

            // when
            boolean verified = jwtTokenUtil.verifyToken(token);

            // then
            assertThat(verified).isTrue();
        }

        @Test
        @DisplayName("过期 Token 应校验失败")
        void should_returnFalse_when_tokenExpired() {
            // given - 设置过期时间为 0 分钟，立即过期
            jwtProperties.setAccessTokenExpireMinutes(0L);
            String token = jwtTokenUtil.generateAccessToken(ADMIN_ID, IDENTIFIER_ADMIN);

            // 等待 1 秒确保 Token 已过期
            try {
                Thread.sleep(1100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // when
            boolean verified = jwtTokenUtil.verifyToken(token);

            // then
            assertThat(verified).isFalse();
        }

        @Test
        @DisplayName("篡改签名的 Token 应校验失败")
        void should_returnFalse_when_tokenSignatureTampered() {
            // given - 修改密钥后重新生成工具实例
            JwtProperties tampered = new JwtProperties();
            tampered.setSecret("another-different-secret-key-32-bytes-long");
            tampered.setIssuer(JWT_ISSUER);
            tampered.setAccessTokenExpireMinutes(ACCESS_TOKEN_EXPIRE_MINUTES);
            tampered.setRefreshTokenExpireDays(REFRESH_TOKEN_EXPIRE_DAYS);

            JwtTokenUtil tamperedUtil = new JwtTokenUtil();
            ReflectionTestUtils.setField(tamperedUtil, "jwtProperties", tampered);

            String tamperedToken = tamperedUtil.generateAccessToken(ADMIN_ID, IDENTIFIER_ADMIN);

            // when - 用原始工具校验"用其他密钥签发"的 Token
            boolean verified = jwtTokenUtil.verifyToken(tamperedToken);

            // then
            assertThat(verified).isFalse();
        }

        @Test
        @DisplayName("格式非法的 Token 字符串应校验失败且不抛异常")
        void should_returnFalse_when_tokenMalformed() {
            // given
            String malformed = "not.a.valid.jwt.token";

            // when
            boolean verified = jwtTokenUtil.verifyToken(malformed);

            // then
            assertThat(verified).isFalse();
        }

        @Test
        @DisplayName("空 Token 字符串应校验失败且不抛异常")
        void should_returnFalse_when_tokenEmpty() {
            // when
            boolean verified = jwtTokenUtil.verifyToken("");

            // then
            assertThat(verified).isFalse();
        }
    }

    @Nested
    @DisplayName("场景: 解析非法 Token")
    class ParseInvalidToken {

        @Test
        @DisplayName("解析非法 Token 的 userId 应返回 null 而非抛异常")
        void should_returnNull_when_parseUserIdFromInvalidToken() {
            // when
            Long userId = jwtTokenUtil.getUserId("invalid-token-string");

            // then
            assertThat(userId).isNull();
        }

        @Test
        @DisplayName("解析非法 Token 的 tokenType 应返回 null 而非抛异常")
        void should_returnNull_when_parseTokenTypeFromInvalidToken() {
            // when
            String tokenType = jwtTokenUtil.getTokenType("invalid-token-string");

            // then
            assertThat(tokenType).isNull();
        }
    }
}
