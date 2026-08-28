package org.dam.web.controller;

import org.dam.common.exception.BizException;
import org.dam.common.enums.ResultCode;
import org.dam.controller.RoleController;
import org.dam.dto.RoleSaveDTO;
import org.dam.service.RoleService;
import org.dam.vo.RoleVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import static org.dam.support.TestConstants.ADMIN_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RoleController @WebMvcTest 切片测试
 * 覆盖详情/新增/参数校验失败/BizException 全局转换
 * AOP 切面不生效，@RequiresPermission 注解不触发权限校验（由集成测试验证）
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
@DisplayName("WebMvc: RoleController - 角色接口")
@Import(RoleController.class)
class RoleControllerTest extends BaseControllerMvcTest {

    @MockBean
    RoleService roleService;

    // =====================================================================
    // 场景组: 查询 GET /role/{id}
    // =====================================================================
    @Nested
    @DisplayName("场景: 角色查询")
    class Query {

        @Test
        @DisplayName("假如 角色 ID=1 存在 当 GET /role/1 那么 HTTP 200 而且返回角色详情")
        void should_returnRoleVO_when_roleExists() throws Exception {
            // given
            RoleVO vo = new RoleVO();
            vo.setId(ADMIN_ID);
            vo.setRoleCode("ADMIN");
            vo.setRoleName("超级管理员");
            given(roleService.getRoleById(ADMIN_ID)).willReturn(vo);

            // when + then
            mockMvc.perform(get("/role/{id}", ADMIN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(ADMIN_ID))
                .andExpect(jsonPath("$.data.roleCode").value("ADMIN"))
                .andExpect(jsonPath("$.data.roleName").value("超级管理员"));
        }
    }

    // =====================================================================
    // 场景组: 新增 POST /role
    // =====================================================================
    @Nested
    @DisplayName("场景: 角色新增")
    class Save {

        @Test
        @DisplayName("假如 提交合法 RoleSaveDTO 当 POST /role 那么 返回新角色 ID")
        void should_returnNewRoleId_when_saveSuccess() throws Exception {
            // given
            given(roleService.saveRole(any(RoleSaveDTO.class))).willReturn(100L);

            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setRoleCode("MANAGER");
            dto.setRoleName("部门经理");
            dto.setDescription("部门管理角色");

            // when + then
            mockMvc.perform(post("/role")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").value(100));
        }

        @Test
        @DisplayName("假如 roleCode 为空 当 POST /role 那么 Result.code=1400 而且消息含\"角色编码不能为空\"")
        void should_returnValidateFailed_when_roleCodeBlank() throws Exception {
            // given
            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setRoleCode("");
            dto.setRoleName("测试角色");

            // when + then
            mockMvc.perform(post("/role")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("角色编码不能为空")));
        }

        @Test
        @DisplayName("假如 roleCode 含非法字符（以数字开头） 当 POST /role 那么 Result.code=1400 而且消息含\"角色编码需以字母开头\"")
        void should_returnValidateFailed_when_roleCodePatternInvalid() throws Exception {
            // given
            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setRoleCode("1INVALID");
            dto.setRoleName("测试角色");

            // when + then
            mockMvc.perform(post("/role")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("角色编码需以字母开头")));
        }
    }

    // =====================================================================
    // 场景组: 删除 DELETE /role/{id}
    // =====================================================================
    @Nested
    @DisplayName("场景: 角色删除")
    class Remove {

        @Test
        @DisplayName("假如 内置角色不可删 当 DELETE /role/{id} 那么 Service 抛 BizException 由全局处理器转换")
        void should_returnBizError_when_removeBuiltIn() throws Exception {
            // given
            given(roleService.removeRole(1L))
                .willThrow(new BizException(1003, "内置角色不允许删除"));

            // when + then
            mockMvc.perform(delete("/role/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003))
                .andExpect(jsonPath("$.message").value("内置角色不允许删除"));

            verify(roleService).removeRole(1L);
        }
    }
}
