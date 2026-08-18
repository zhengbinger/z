package org.dam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户角色视图对象
 * 用于返回用户已分配的角色列表
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "用户角色响应")
public class UserRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 已分配的角色集合
     */
    @Schema(description = "已分配的角色集合")
    private List<RoleVO> roles;

}
