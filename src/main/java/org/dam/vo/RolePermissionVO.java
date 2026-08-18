package org.dam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色权限视图对象
 * 用于返回角色已分配的权限列表，以及未分配的权限列表（便于前端勾选展示）
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "角色权限响应")
public class RolePermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID
     */
    @Schema(description = "角色 ID")
    private Long roleId;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码")
    private String roleCode;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 已分配的权限集合
     */
    @Schema(description = "已分配的权限集合")
    private List<PermissionVO> assignedPermissions;

    /**
     * 未分配的权限集合
     */
    @Schema(description = "未分配的权限集合")
    private List<PermissionVO> unassignedPermissions;

}
