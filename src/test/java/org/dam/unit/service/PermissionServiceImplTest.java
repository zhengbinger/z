package org.dam.unit.service;

import org.dam.common.exception.BizException;
import org.dam.dto.PermissionSaveDTO;
import org.dam.entity.Permission;
import org.dam.mapper.PermissionMapper;
import org.dam.mapper.RolePermissionMapper;
import org.dam.service.impl.PermissionServiceImpl;
import org.dam.support.TestDataBuilder;
import org.dam.vo.PermissionVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.dam.support.TestConstants.PERM_USER_LIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * PermissionServiceImpl 单元测试
 * 覆盖权限 CRUD 与级联删除逻辑
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
@DisplayName("功能: PermissionService - 权限管理")
@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock private PermissionMapper permissionMapper;
    @Mock private RolePermissionMapper rolePermissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Nested
    @DisplayName("场景: 查询权限详情")
    class GetPermissionById {

        @Test
        @DisplayName("权限存在时返回 PermissionVO")
        void should_returnPermissionVO_when_permissionExists() {
            // given
            Permission permission = TestDataBuilder.permission().build();
            given(permissionMapper.selectById(1L)).willReturn(permission);

            // when
            PermissionVO vo = permissionService.getPermissionById(1L);

            // then
            assertThat(vo.getPermissionCode()).isEqualTo(PERM_USER_LIST);
        }

        @Test
        @DisplayName("权限不存在应抛\"权限不存在\"")
        void should_throwNotFound_when_permissionNotExists() {
            // given
            given(permissionMapper.selectById(anyLong())).willReturn(null);

            // when + then
            assertThatThrownBy(() -> permissionService.getPermissionById(99L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("权限不存在");
        }
    }

    @Nested
    @DisplayName("场景: 新增权限")
    class SavePermission {

        @Test
        @DisplayName("权限编码已存在应抛\"权限编码已存在\"")
        void should_throwBizError_when_codeDuplicated() {
            // given
            given(permissionMapper.selectCount(any())).willReturn(1L);
            PermissionSaveDTO dto = new PermissionSaveDTO();
            dto.setPermissionCode(PERM_USER_LIST);
            dto.setPermissionName("用户查询");
            dto.setType(3);

            // when + then
            assertThatThrownBy(() -> permissionService.savePermission(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("权限编码已存在");
            verify(permissionMapper, never()).insert(any());
        }

        @Test
        @DisplayName("编码唯一时插入并自动补齐默认值")
        void should_insertWithDefaults_when_codeUnique() {
            // given
            given(permissionMapper.selectCount(any())).willReturn(0L);
            PermissionSaveDTO dto = new PermissionSaveDTO();
            dto.setPermissionCode("user:export");
            dto.setPermissionName("用户导出");
            dto.setType(3);

            // when
            Long id = permissionService.savePermission(dto);

            // then
            verify(permissionMapper, times(1)).insert(any(Permission.class));
            assertThat(id).isNull();
        }
    }

    @Nested
    @DisplayName("场景: 删除权限")
    class RemovePermission {

        @Test
        @DisplayName("权限存在应级联删除角色关联并返回 true")
        void should_cascadeDeleteRoleAssociations_when_permissionExists() {
            // given
            Permission permission = TestDataBuilder.permission().build();
            given(permissionMapper.selectById(1L)).willReturn(permission);
            given(permissionMapper.deleteById(1L)).willReturn(1);
            given(rolePermissionMapper.delete(any())).willReturn(2);

            // when
            Boolean result = permissionService.removePermission(1L);

            // then
            assertThat(result).isTrue();
            verify(permissionMapper, times(1)).deleteById(1L);
            verify(rolePermissionMapper, times(1)).delete(any());
        }

        @Test
        @DisplayName("权限不存在应抛\"权限不存在\"")
        void should_throwNotFound_when_permissionNotExists() {
            // given
            given(permissionMapper.selectById(99L)).willReturn(null);

            // when + then
            assertThatThrownBy(() -> permissionService.removePermission(99L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("权限不存在");
            verify(permissionMapper, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("场景: 修改权限")
    class UpdatePermission {

        @Test
        @DisplayName("ID 为空应抛\"权限 ID 不能为空\"")
        void should_throwParamError_when_idNull() {
            // given
            PermissionSaveDTO dto = new PermissionSaveDTO();
            dto.setPermissionCode(PERM_USER_LIST);

            // when + then
            assertThatThrownBy(() -> permissionService.updatePermission(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("权限 ID 不能为空");
        }

        @Test
        @DisplayName("权限不存在应抛\"权限不存在\"")
        void should_throwNotFound_when_permissionNotExists() {
            // given
            given(permissionMapper.selectById(anyLong())).willReturn(null);
            PermissionSaveDTO dto = new PermissionSaveDTO();
            dto.setId(99L);
            dto.setPermissionCode(PERM_USER_LIST);
            dto.setPermissionName("用户查询");
            dto.setType(3);

            // when + then
            assertThatThrownBy(() -> permissionService.updatePermission(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("权限不存在");
            verify(permissionMapper, never()).updateById(any());
        }
    }
}
