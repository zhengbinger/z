package org.dam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 角色分配权限 DTO
 * 用全量覆盖策略：传入的 permissionIds 即为该角色的最终权限集合，
 * 服务端会移除不在列表中的旧关联，并补齐新关联
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "角色分配权限请求")
public class RolePermissionAssignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID
     */
    @Schema(description = "角色 ID", required = true)
    @NotNull(message = "角色 ID 不能为空")
    private Long roleId;

    /**
     * 权限 ID 集合（全量覆盖，可为空集合表示清空所有权限）
     */
    @Schema(description = "权限 ID 集合（全量覆盖）")
    @NotEmpty(message = "权限 ID 集合不能为空")
    private List<Long> permissionIds;

}
