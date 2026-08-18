package org.dam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 用户分配角色 DTO
 * 用全量覆盖策略：传入的 roleIds 即为该用户的最终角色集合，
 * 服务端会移除不在列表中的旧关联，并补齐新关联
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "用户分配角色请求")
public class UserRoleAssignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID", required = true)
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /**
     * 角色 ID 集合（全量覆盖，可为空集合表示清空所有角色）
     */
    @Schema(description = "角色 ID 集合（全量覆盖）")
    @NotEmpty(message = "角色 ID 集合不能为空")
    private List<Long> roleIds;

}
