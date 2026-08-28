package org.dam.web.controller;

import org.dam.common.exception.BizException;
import org.dam.common.enums.ResultCode;
import org.dam.controller.PermissionController;
import org.dam.dto.PermissionSaveDTO;
import org.dam.service.PermissionService;
import org.dam.vo.PermissionVO;
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
 * PermissionController @WebMvcTest 切片测试
 * 覆盖详情/新增/参数校验失败/BizException 全局转换
 * AOP 切面不生效，@RequiresPermission 注解不触发权限校验（由集成测试验证）
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
@DisplayName("WebMvc: PermissionController - 权限接口")
@Import(PermissionController.class)
class PermissionControllerTest extends BaseControllerMvcTest {

    @MockBean
    PermissionService permissionService;

    // =====================================================================
    // 场景组: 查询 GET /permission/{id}
    // =====================================================================
    @Nested
    @DisplayName("场景: 权限查询")
    class Query {

        @Test
        @DisplayName("假如 权限 ID=1 存在 当 GET /permission/1 那么 HTTP 200 而且返回权限详情")
        void should_returnPermissionVO_when_permissionExists() throws Exception {
            // given
            PermissionVO vo = new PermissionVO();
            vo.setId(ADMIN_ID);
            vo.setPermissionCode("user:list");
            vo.setPermissionName("用户查询");
            vo.setType(3);
            given(permissionService.getPermissionById(ADMIN_ID)).willReturn(vo);

            // when + then
            mockMvc.perform(get("/permission/{id}", ADMIN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(ADMIN_ID))
                .andExpect(jsonPath("$.data.permissionCode").value("user:list"))
                .andExpect(jsonPath("$.data.permissionName").value("用户查询"));
        }
    }

    // =====================================================================
    // 场景组: 新增 POST /permission
    // =====================================================================
    @Nested
    @DisplayName("场景: 权限新增")
    class Save {

        @Test
        @DisplayName("假如 提交合法 PermissionSaveDTO 当 POST /permission 那么 返回新权限 ID")
        void should_returnNewPermissionId_when_saveSuccess() throws Exception {
            // given
            given(permissionService.savePermission(any(PermissionSaveDTO.class))).willReturn(100L);

            PermissionSaveDTO dto = new PermissionSaveDTO();
            dto.setPermissionCode("user:export");
            dto.setPermissionName("用户导出");
            dto.setType(3);

            // when + then
            mockMvc.perform(post("/permission")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").value(100));
        }

        @Test
        @DisplayName("假如 permissionCode 为空 当 POST /permission 那么 Result.code=1400 而且消息含\"权限编码不能为空\"")
        void should_returnValidateFailed_when_permissionCodeBlank() throws Exception {
            // given
            PermissionSaveDTO dto = new PermissionSaveDTO();
            dto.setPermissionCode("");
            dto.setPermissionName("用户查询");
            dto.setType(3);

            // when + then
            mockMvc.perform(post("/permission")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("权限编码不能为空")));
        }

        @Test
        @DisplayName("假如 type 为空 当 POST /permission 那么 Result.code=1400 而且消息含\"权限类型不能为空\"")
        void should_returnValidateFailed_when_typeNull() throws Exception {
            // given
            PermissionSaveDTO dto = new PermissionSaveDTO();
            dto.setPermissionCode("user:list");
            dto.setPermissionName("用户查询");
            // type 不设置

            // when + then
            mockMvc.perform(post("/permission")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.PARAM_VALIDATE_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("权限类型不能为空")));
        }
    }

    // =====================================================================
    // 场景组: 删除 DELETE /permission/{id}
    // =====================================================================
    @Nested
    @DisplayName("场景: 权限删除")
    class Remove {

        @Test
        @DisplayName("假如 权限被角色引用 当 DELETE /permission/{id} 那么 Service 抛 BizException 由全局处理器转换")
        void should_returnBizError_when_permissionInUse() throws Exception {
            // given
            given(permissionService.removePermission(1L))
                .willThrow(new BizException(1004, "权限已被角色引用，无法删除"));

            // when + then
            mockMvc.perform(delete("/permission/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1004))
                .andExpect(jsonPath("$.message").value("权限已被角色引用，无法删除"));

            verify(permissionService).removePermission(1L);
        }
    }
}
