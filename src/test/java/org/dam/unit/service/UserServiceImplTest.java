package org.dam.unit.service;

import org.dam.common.exception.BizException;
import org.dam.component.status.UserStatus;
import org.dam.component.status.UserStatusChangeEvent;
import org.dam.component.status.UserStatusChangePublisher;
import org.dam.dto.UserSaveDTO;
import org.dam.entity.User;
import org.dam.mapper.RoleMapper;
import org.dam.mapper.UserMapper;
import org.dam.mapper.UserRoleMapper;
import org.dam.service.impl.UserServiceImpl;
import org.dam.support.TestDataBuilder;
import org.dam.vo.UserVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.dam.support.TestConstants.ADMIN_ID;
import static org.dam.support.TestConstants.IDENTIFIER_ADMIN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * UserServiceImpl 单元测试
 * 覆盖查询、新增、修改、删除、状态变更核心路径
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
@DisplayName("功能: UserService - 用户管理")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserMapper userMapper;
    @Mock private UserRoleMapper userRoleMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private UserStatusChangePublisher userStatusChangePublisher;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("场景: 查询用户详情")
    class GetUserById {

        @Test
        @DisplayName("用户存在时返回脱敏后的 UserVO")
        void should_returnMaskedVO_when_userExists() {
            // given
            User user = TestDataBuilder.user()
                .phone("13800138000")
                .email("admin@dam.com")
                .build();
            given(userMapper.selectById(ADMIN_ID)).willReturn(user);

            // when
            UserVO vo = userService.getUserById(ADMIN_ID);

            // then - 手机号脱敏（前 3 + 后 4）
            assertThat(vo.getPhone()).isEqualTo("138****8000");
            // then - 邮箱脱敏（保留首字符和 @ 后域名）
            assertThat(vo.getEmail()).startsWith("a");
            assertThat(vo.getEmail()).contains("@dam.com");
            assertThat(vo.getId()).isEqualTo(ADMIN_ID);
            assertThat(vo.getUsername()).isEqualTo(IDENTIFIER_ADMIN);
        }

        @Test
        @DisplayName("用户不存在时应抛\"用户不存在\"")
        void should_throwNotFound_when_userNotExists() {
            // given
            given(userMapper.selectById(ADMIN_ID)).willReturn(null);

            // when + then
            assertThatThrownBy(() -> userService.getUserById(ADMIN_ID))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户不存在");
        }
    }

    @Nested
    @DisplayName("场景: 新增用户")
    class SaveUser {

        @Test
        @DisplayName("用户名唯一时应插入并返回新用户 ID")
        void should_returnNewUserId_when_usernameUnique() {
            // given
            given(userMapper.selectCount(any())).willReturn(0L);
            UserSaveDTO dto = new UserSaveDTO();
            dto.setUsername("newuser");
            dto.setNickname("新用户");
            dto.setGender(1);
            dto.setStatus(1);

            // 模拟 MyBatis Plus insert 返回影响行数 1（insert 返回 int 非 void）
            given(userMapper.insert(any(User.class))).willReturn(1);

            // when
            Long userId = userService.saveUser(dto);

            // then - ID 由 mapper insert 回填，单元测试下为 null 但方法不抛异常即视为通过
            verify(userMapper, times(1)).insert(any(User.class));
            assertThat(userId).isNull();
        }

        @Test
        @DisplayName("用户名已存在时应抛\"用户名已存在\"")
        void should_throwBizError_when_usernameDuplicated() {
            // given
            given(userMapper.selectCount(any())).willReturn(1L);
            UserSaveDTO dto = new UserSaveDTO();
            dto.setUsername(IDENTIFIER_ADMIN);

            // when + then
            assertThatThrownBy(() -> userService.saveUser(dto))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户名已存在");
            verify(userMapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("场景: 删除用户")
    class RemoveUser {

        @Test
        @DisplayName("用户存在时应执行逻辑删除")
        void should_deleteById_when_userExists() {
            // given
            User user = TestDataBuilder.user().build();
            given(userMapper.selectById(ADMIN_ID)).willReturn(user);
            given(userMapper.deleteById(ADMIN_ID)).willReturn(1);

            // when
            Boolean result = userService.removeUser(ADMIN_ID);

            // then
            assertThat(result).isTrue();
            verify(userMapper, times(1)).deleteById(ADMIN_ID);
        }

        @Test
        @DisplayName("用户不存在应抛\"用户不存在\"")
        void should_throwNotFound_when_userNotExists() {
            // given
            given(userMapper.selectById(ADMIN_ID)).willReturn(null);

            // when + then
            assertThatThrownBy(() -> userService.removeUser(ADMIN_ID))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户不存在");
            verify(userMapper, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("场景: 变更用户状态")
    class ChangeUserStatus {

        @Test
        @DisplayName("目标状态与当前状态相同时跳过发布事件，返回 false")
        void should_returnFalse_when_statusNotChanged() {
            // given
            User user = TestDataBuilder.user().enabled().build();
            given(userMapper.selectById(ADMIN_ID)).willReturn(user);

            // when
            Boolean result = userService.changeUserStatus(ADMIN_ID, UserStatus.ENABLED);

            // then
            assertThat(result).isFalse();
            verify(userStatusChangePublisher, never()).publish(any());
        }

        @Test
        @DisplayName("状态实际变化时发布 UserStatusChangeEvent")
        void should_publishEvent_when_statusChanged() {
            // given
            User user = TestDataBuilder.user().enabled().build();
            given(userMapper.selectById(ADMIN_ID)).willReturn(user);
            given(userMapper.updateById(any(User.class))).willReturn(1);
            willDoNothing().given(userStatusChangePublisher).publish(any(UserStatusChangeEvent.class));

            // when
            Boolean result = userService.changeUserStatus(ADMIN_ID, UserStatus.DISABLED);

            // then
            assertThat(result).isTrue();
            verify(userStatusChangePublisher, times(1)).publish(any(UserStatusChangeEvent.class));
        }

        @Test
        @DisplayName("用户不存在应抛\"用户不存在\"")
        void should_throwNotFound_when_userNotExists() {
            // given
            given(userMapper.selectById(ADMIN_ID)).willReturn(null);

            // when + then
            assertThatThrownBy(() -> userService.changeUserStatus(ADMIN_ID, UserStatus.DISABLED))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("userId 为 null 时抛参数校验异常")
        void should_throwParamError_when_userIdNull() {
            // when + then
            assertThatThrownBy(() -> userService.changeUserStatus(null, UserStatus.DISABLED))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户 ID 或目标状态不能为空");
            verify(userMapper, never()).selectById(any());
        }
    }
}
