package org.dam.unit.service;

import org.dam.common.exception.BizException;
import org.dam.dto.RoleSaveDTO;
import org.dam.entity.Role;
import org.dam.mapper.PermissionMapper;
import org.dam.mapper.RoleMapper;
import org.dam.mapper.RolePermissionMapper;
import org.dam.service.impl.RoleServiceImpl;
import org.dam.support.TestDataBuilder;
import org.dam.vo.RoleVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.dam.support.TestConstants.ROLE_ADMIN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * RoleServiceImpl 单元测试
 * 覆盖角色 CRUD 与内置角色保护逻辑
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
@DisplayName("功能: RoleService - 角色管理")
@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock private RoleMapper roleMapper;
    @Mock private RolePermissionMapper rolePermissionMapper;
    @Mock private PermissionMapper permissionMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Nested
    @DisplayName("场景: 查询角色详情")
    class GetRoleById {

        @Test
        @DisplayName("角色存在时返回 RoleVO")
        void should_returnRoleVO_when_roleExists() {
            // given
            Role role = TestDataBuilder.role().builtIn().build();
            given(roleMapper.selectById(1L)).willReturn(role);

            // when
            RoleVO vo = roleService.getRoleById(1L);

            // then
            assertThat(vo.getRoleCode()).isEqualTo(ROLE_ADMIN);
        }

        @Test
        @DisplayName("角色不存在应抛\"角色不存在\"")
        void should_throwNotFound_when_roleNotExists() {
            // given
            given(roleMapper.selectById(anyLong())).willReturn(null);

            // when + then
            assertThatThrownBy(() -> roleService.getRoleById(99L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("角色不存在");
        }
    }

    @Nested
    @DisplayName("场景: 删除角色")
    class RemoveRole {

        @Test
        @DisplayName("内置角色不允许删除")
        void should_throwBizError_when_roleIsBuiltIn() {
            // given
            Role builtIn = TestDataBuilder.role().builtIn().build();
            given(roleMapper.selectById(1L)).willReturn(builtIn);

            // when + then
            assertThatThrownBy(() -> roleService.removeRole(1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("内置角色不允许删除");
            verify(roleMapper, never()).deleteById(any());
        }

        @Test
        @DisplayName("普通角色应级联删除关联权限并返回 true")
        void should_cascadeDeletePermissions_when_roleIsNotBuiltIn() {
            // given
            Role role = TestDataBuilder.role().notBuiltIn().build();
            given(roleMapper.selectById(1L)).willReturn(role);
            given(roleMapper.deleteById(1L)).willReturn(1);
            given(rolePermissionMapper.delete(any())).willReturn(1);

            // when
            Boolean result = roleService.removeRole(1L);

            // then
            assertThat(result).isTrue();
            verify(roleMapper, times(1)).deleteById(1L);
            verify(rolePermissionMapper, times(1)).delete(any());
        }

        @Test
        @DisplayName("角色不存在应抛\"角色不存在\"")
        void should_throwNotFound_when_roleNotExists() {
            // given
            given(roleMapper.selectById(99L)).willReturn(null);

            // when + then
            assertThatThrownBy(() -> roleService.removeRole(99L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("角色不存在");
        }
    }

    @Nested
    @DisplayName("场景: 新增角色")
    class SaveRole {

        @Test
        @DisplayName("角色编码已存在应抛\"角色编码已存在\"")
        void should_throwBizError_when_roleCodeDuplicated() {
            // given
            given(roleMapper.selectCount(any())).willReturn(1L);
            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setRoleCode(ROLE_ADMIN);
            dto.setRoleName("超级管理员");

            // when + then
            assertThatThrownBy(() -> roleService.saveRole(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("角色编码已存在");
            verify(roleMapper, never()).insert(any());
        }

        @Test
        @DisplayName("编码唯一时插入新角色并默认设为非内置")
        void should_insertRole_when_codeUnique() {
            // given
            given(roleMapper.selectCount(any())).willReturn(0L);
            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setRoleCode("MANAGER");
            dto.setRoleName("经理");

            // when
            Long id = roleService.saveRole(dto);

            // then
            verify(roleMapper, times(1)).insert(any(Role.class));
            assertThat(id).isNull();
        }
    }

    @Nested
    @DisplayName("场景: 修改角色")
    class UpdateRole {

        @Test
        @DisplayName("ID 为空应抛\"角色 ID 不能为空\"")
        void should_throwParamError_when_idNull() {
            // given
            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setRoleCode(ROLE_ADMIN);

            // when + then
            assertThatThrownBy(() -> roleService.updateRole(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("角色 ID 不能为空");
        }

        @Test
        @DisplayName("内置角色修改 roleCode 应抛\"内置角色编码不允许修改\"")
        void should_throwBizError_when_modifyBuiltInRoleCode() {
            // given
            Role builtIn = TestDataBuilder.role().builtIn().build();
            given(roleMapper.selectById(1L)).willReturn(builtIn);
            given(roleMapper.selectCount(any())).willReturn(0L);

            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setId(1L);
            dto.setRoleCode("NEW_ADMIN");
            dto.setRoleName("新管理员");

            // when + then
            assertThatThrownBy(() -> roleService.updateRole(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("内置角色编码不允许修改");
            verify(roleMapper, never()).updateById(any());
        }
    }
}
