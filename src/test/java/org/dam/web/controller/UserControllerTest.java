package org.dam.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dam.common.exception.BizException;
import org.dam.common.enums.ResultCode;
import org.dam.controller.UserController;
import org.dam.dto.UserSaveDTO;
import org.dam.service.DictService;
import org.dam.service.UserService;
import org.dam.vo.UserVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.Collections;

import static org.dam.support.TestConstants.ADMIN_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserController Web 切片测试
 * 覆盖分页/详情/新增/状态变更/删除 + 参数校验失败 + BizException 全局转换
 * AOP 切面不生效，@RequiresPermission 注解不触发权限校验（由集成测试验证）
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
@DisplayName("WebMvc: UserController - 用户接口")
@Import(UserController.class)
class UserControllerTest extends BaseControllerMvcTest {

    @MockBean
    private UserService userService;

    // @DictValidator 校验器通过 SpringConstraintValidatorFactory 注入 DictService
    // 切片测试不扫描 service 包，需提供 mock bean 否则验证器实例化抛 NoSuchBeanDefinitionException
    @MockBean
    private DictService dictService;

    // =====================================================================
    // 场景组: 查询 GET
    // =====================================================================
    @Nested
    @DisplayName("场景: 用户查询")
    class Query {

        @Test
        @DisplayName("假如 用户 ID=1 存在 当 GET /user/1 那么 HTTP 200 而且返回脱敏后的用户信息")
        void should_returnUserVO_when_userExists() throws Exception {
            // given
            UserVO vo = new UserVO();
            vo.setId(ADMIN_ID);
            vo.setUsername("admin");
            vo.setPhone("138****8000");
            given(userService.getUserById(ADMIN_ID)).willReturn(vo);

            // when + then
            mockMvc.perform(get("/user/{id}", ADMIN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(ADMIN_ID))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.phone").value("138****8000"));
        }

        @Test
        @DisplayName("假如 用户不存在 当 GET /user/{id} 那么 Service 抛 BizException 由全局处理器转换")
        void should_returnBizError_when_userNotFound() throws Exception {
            // given
            given(userService.getUserById(99L))
                .willThrow(new BizException(ResultCode.NOT_FOUND));

            // when + then
            mockMvc.perform(get("/user/{id}", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ResultCode.NOT_FOUND.getMessage()));
        }
    }

    // =====================================================================
    // 场景组: 新增 POST
    // =====================================================================
    @Nested
    @DisplayName("场景: 用户新增")
    class Save {

        @Test
        @DisplayName("假如 提交合法 UserSaveDTO 当 POST /user 那么 返回新用户 ID")
        void should_returnNewUserId_when_saveSuccess() throws Exception {
            // given
            given(userService.saveUser(any(UserSaveDTO.class))).willReturn(100L);
            // @DictValidator 校验 status=1 是否在 common_status 字典合法范围内
            given(dictService.isValidValue("common_status", "1")).willReturn(true);

            UserSaveDTO dto = new UserSaveDTO();
            dto.setUsername("newuser");
            dto.setNickname("新用户");
            dto.setGender(1);
            dto.setStatus(1);

            // when + then
            mockMvc.perform(post("/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").value(100));
        }

        @Test
        @DisplayName("假如 username 为空 当 POST /user 那么 Result.code=1400 而且消息含\"用户名不能为空\"")
        void should_returnValidateFailed_when_usernameBlank() throws Exception {
            // given
            UserSaveDTO dto = new UserSaveDTO();
            dto.setUsername("");
            dto.setNickname("新用户");

            // when + then
            mockMvc.perform(post("/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户名不能为空")));
        }
    }

    // =====================================================================
    // 场景组: 状态变更 PUT /user/{id}/status
    // =====================================================================
    @Nested
    @DisplayName("场景: 用户状态变更")
    class ChangeStatus {

        @Test
        @DisplayName("假如 status=1（启用） 当 PUT /user/{id}/status?status=1 那么 调用 Service 而且 Result.data=true")
        void should_changeStatus_when_statusValid() throws Exception {
            // given
            given(userService.changeUserStatus(eq(ADMIN_ID), any())).willReturn(Boolean.TRUE);

            // when + then
            mockMvc.perform(put("/user/{id}/status", ADMIN_ID)
                    .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("假如 status=9（非法状态码） 当 PUT /user/{id}/status 那么 Result.code=1400 而且消息含\"非法状态码\"")
        void should_returnValidateFailed_when_statusInvalid() throws Exception {
            // when + then - Controller 内部校验，不走 Service
            mockMvc.perform(put("/user/{id}/status", ADMIN_ID)
                    .param("status", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("非法状态码")));
        }
    }

    // =====================================================================
    // 场景组: 删除 DELETE /user/{id}
    // =====================================================================
    @Nested
    @DisplayName("场景: 用户删除")
    class Remove {

        @Test
        @DisplayName("假如 Service 抛 BizException 当 DELETE /user/{id} 那么 全局处理器返回业务码")
        void should_returnBizError_when_serviceThrows() throws Exception {
            // given
            willThrow(new BizException(1002, "用户不存在"))
                .given(userService).removeUser(99L);

            // when + then
            mockMvc.perform(delete("/user/{id}", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.message").value("用户不存在"));
        }
    }

    // =====================================================================
    // 场景组: 分页 POST /user/page
    // =====================================================================
    @Nested
    @DisplayName("场景: 用户分页")
    class Pagination {

        @Test
        @DisplayName("假如 提交合法分页参数 当 POST /user/page 那么 返回分页结果")
        void should_returnPage_when_queryValid() throws Exception {
            // given
            Page<UserVO> page = new Page<>(1, 10);
            page.setTotal(1L);
            UserVO vo = new UserVO();
            vo.setId(ADMIN_ID);
            vo.setUsername("admin");
            page.setRecords(Collections.singletonList(vo));
            given(userService.pageUser(any())).willReturn(page);

            // when + then
            mockMvc.perform(post("/user/page")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"current\":1,\"size\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("admin"));
        }
    }
}
