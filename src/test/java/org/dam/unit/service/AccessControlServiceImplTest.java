package org.dam.unit.service;

import org.dam.entity.Permission;
import org.dam.entity.Role;
import org.dam.mapper.PermissionMapper;
import org.dam.mapper.RoleMapper;
import org.dam.service.impl.AccessControlServiceImpl;
import org.dam.support.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dam.support.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * AccessControlServiceImpl 单元测试
 * 覆盖 RBAC 查询与判断逻辑
 * @Cacheable 在纯 Mockito 测试不触发（无 Spring 代理），由集成测试验证缓存命中
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
@DisplayName("功能: AccessControlService - RBAC 访问控制")
@ExtendWith(MockitoExtension.class)
class AccessControlServiceImplTest {

    @Mock private RoleMapper roleMapper;
    @Mock private PermissionMapper permissionMapper;

    @InjectMocks
    private AccessControlServiceImpl accessControlService;

    // =====================================================================
    // 场景组: listRoleCodesByUserId
    // =====================================================================
    @Nested
    @DisplayName("场景: 查询用户角色编码列表")
    class ListRoleCodes {

        @Test
        @DisplayName("userId 为 null 时返回空集且不查询 Mapper")
        void should_returnEmptyList_when_userIdIsNull() {
            // when
            List<String> codes = accessControlService.listRoleCodesByUserId(null);

            // then
            assertThat(codes).isEmpty();
            verify(roleMapper, never()).selectRolesByUserId(any());
        }

        @Test
        @DisplayName("用户拥有多个角色应返回角色编码列表")
        void should_returnRoleCodes_when_userHasRoles() {
            // given
            Role admin = TestDataBuilder.role().id(1L).code(ROLE_ADMIN).build();
            Role user = TestDataBuilder.role().id(2L).code(ROLE_USER).build();
            given(roleMapper.selectRolesByUserId(ADMIN_ID))
                .willReturn(Arrays.asList(admin, user));

            // when
            List<String> codes = accessControlService.listRoleCodesByUserId(ADMIN_ID);

            // then
            assertThat(codes).containsExactly(ROLE_ADMIN, ROLE_USER);
        }

        @Test
        @DisplayName("角色编码为空字符串应被过滤")
        void should_filterOutBlankRoleCodes() {
            // given
            Role withBlankCode = new Role();
            withBlankCode.setRoleCode("");
            Role valid = TestDataBuilder.role().id(2L).code(ROLE_USER).build();
            given(roleMapper.selectRolesByUserId(ADMIN_ID))
                .willReturn(Arrays.asList(withBlankCode, valid));

            // when
            List<String> codes = accessControlService.listRoleCodesByUserId(ADMIN_ID);

            // then
            assertThat(codes).containsExactly(ROLE_USER);
        }

        @Test
        @DisplayName("用户无角色时返回空集")
        void should_returnEmptyList_when_userHasNoRoles() {
            // given
            given(roleMapper.selectRolesByUserId(ADMIN_ID))
                .willReturn(Collections.emptyList());

            // when
            List<String> codes = accessControlService.listRoleCodesByUserId(ADMIN_ID);

            // then
            assertThat(codes).isEmpty();
        }
    }

    // =====================================================================
    // 场景组: listPermissionCodesByUserId
    // =====================================================================
    @Nested
    @DisplayName("场景: 查询用户权限编码列表")
    class ListPermissionCodes {

        @Test
        @DisplayName("userId 为 null 时返回空集且不查询 Mapper")
        void should_returnEmptyList_when_userIdIsNull() {
            // when
            List<String> codes = accessControlService.listPermissionCodesByUserId(null);

            // then
            assertThat(codes).isEmpty();
            verify(permissionMapper, never()).selectPermissionsByUserId(any());
        }

        @Test
        @DisplayName("用户拥有多个权限应返回权限编码列表")
        void should_returnPermissionCodes_when_userHasPermissions() {
            // given
            Permission p1 = TestDataBuilder.permission().id(1L).code(PERM_USER_LIST).build();
            Permission p2 = TestDataBuilder.permission().id(2L).code(PERM_USER_GET).build();
            given(permissionMapper.selectPermissionsByUserId(ADMIN_ID))
                .willReturn(Arrays.asList(p1, p2));

            // when
            List<String> codes = accessControlService.listPermissionCodesByUserId(ADMIN_ID);

            // then
            assertThat(codes).containsExactly(PERM_USER_LIST, PERM_USER_GET);
        }
    }

    // =====================================================================
    // 场景组: hasRole / hasPermission
    // =====================================================================
    @Nested
    @DisplayName("场景: 单角色/权限判断")
    class HasSingleCode {

        @Test
        @DisplayName("userId 为 null 时 hasRole 应返回 false")
        void should_returnFalse_when_hasRoleAndUserIdNull() {
            // when
            boolean result = accessControlService.hasRole(null, ROLE_ADMIN);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("roleCode 为空时 hasRole 应返回 false")
        void should_returnFalse_when_hasRoleAndCodeBlank() {
            // when
            boolean result = accessControlService.hasRole(ADMIN_ID, "");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("用户拥有该角色应返回 true")
        void should_returnTrue_when_userHasRole() {
            // given
            Role role = TestDataBuilder.role().code(ROLE_ADMIN).build();
            given(roleMapper.selectRolesByUserId(ADMIN_ID))
                .willReturn(Collections.singletonList(role));

            // when
            boolean result = accessControlService.hasRole(ADMIN_ID, ROLE_ADMIN);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("用户不拥有该角色应返回 false")
        void should_returnFalse_when_userDoesNotHaveRole() {
            // given
            Role role = TestDataBuilder.role().code(ROLE_USER).build();
            given(roleMapper.selectRolesByUserId(ADMIN_ID))
                .willReturn(Collections.singletonList(role));

            // when
            boolean result = accessControlService.hasRole(ADMIN_ID, ROLE_ADMIN);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("userId 为 null 时 hasPermission 应返回 false")
        void should_returnFalse_when_hasPermissionAndUserIdNull() {
            // when
            boolean result = accessControlService.hasPermission(null, PERM_USER_LIST);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("permissionCode 为空时 hasPermission 应返回 false")
        void should_returnFalse_when_hasPermissionAndCodeBlank() {
            // when
            boolean result = accessControlService.hasPermission(ADMIN_ID, "");

            // then
            assertThat(result).isFalse();
        }
    }

    // =====================================================================
    // 场景组: hasAnyRole / hasAnyPermission
    // =====================================================================
    @Nested
    @DisplayName("场景: 多角色/权限任一匹配")
    class HasAnyCode {

        @Test
        @DisplayName("userId 为 null 时 hasAnyRole 应返回 false")
        void should_returnFalse_when_hasAnyRoleAndUserIdNull() {
            // when
            boolean result = accessControlService.hasAnyRole(null, ROLE_ADMIN, ROLE_USER);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("roleCodes 为空数组时 hasAnyRole 应返回 false")
        void should_returnFalse_when_hasAnyRoleAndCodesEmpty() {
            // when
            boolean result = accessControlService.hasAnyRole(ADMIN_ID);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("用户角色包含任一查询角色应返回 true")
        void should_returnTrue_when_userHasAnyRole() {
            // given
            Role userRole = TestDataBuilder.role().code(ROLE_USER).build();
            given(roleMapper.selectRolesByUserId(USER_ID))
                .willReturn(Collections.singletonList(userRole));

            // when
            boolean result = accessControlService.hasAnyRole(USER_ID, ROLE_ADMIN, ROLE_USER);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("用户角色都不包含时应返回 false")
        void should_returnFalse_when_userHasNoMatchingRole() {
            // given
            Role userRole = TestDataBuilder.role().code(ROLE_USER).build();
            given(roleMapper.selectRolesByUserId(USER_ID))
                .willReturn(Collections.singletonList(userRole));

            // when
            boolean result = accessControlService.hasAnyRole(USER_ID, ROLE_ADMIN, ROLE_UNKNOWN);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("查询角色中含空白字符串应被忽略")
        void should_skipBlankCodes_when_hasAnyRole() {
            // given
            Role userRole = TestDataBuilder.role().code(ROLE_USER).build();
            given(roleMapper.selectRolesByUserId(USER_ID))
                .willReturn(Collections.singletonList(userRole));

            // when - 包含空白和 null（数组中）
            boolean result = accessControlService.hasAnyRole(USER_ID, "", ROLE_USER);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("userId 为 null 时 hasAnyPermission 应返回 false")
        void should_returnFalse_when_hasAnyPermissionAndUserIdNull() {
            // when
            boolean result = accessControlService.hasAnyPermission(null, PERM_USER_LIST);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("permissionCodes 为空数组时 hasAnyPermission 应返回 false")
        void should_returnFalse_when_hasAnyPermissionAndCodesEmpty() {
            // when
            boolean result = accessControlService.hasAnyPermission(ADMIN_ID);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("用户权限包含任一查询权限应返回 true")
        void should_returnTrue_when_userHasAnyPermission() {
            // given
            Permission p = TestDataBuilder.permission().code(PERM_USER_LIST).build();
            given(permissionMapper.selectPermissionsByUserId(USER_ID))
                .willReturn(Collections.singletonList(p));

            // when
            boolean result = accessControlService.hasAnyPermission(
                USER_ID, PERM_ROLE_LIST, PERM_USER_LIST, PERM_UNKNOWN);

            // then
            assertThat(result).isTrue();
        }
    }
}
