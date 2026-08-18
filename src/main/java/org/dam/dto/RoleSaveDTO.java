package org.dam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 角色新增/修改 DTO
 * 用于接收前端提交的角色表单数据
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "角色新增/修改请求")
public class RoleSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID（新增时不传，修改时必传）
     */
    @Schema(description = "主键 ID（修改时必传）")
    private Long id;

    /**
     * 角色编码（程序使用，唯一）
     */
    @Schema(description = "角色编码", required = true)
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过 50 个字符")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_:]*$", message = "角色编码需以字母开头，仅含字母、数字、下划线、冒号")
    private String roleCode;

    /**
     * 角色名称（展示用）
     */
    @Schema(description = "角色名称", required = true)
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过 50 个字符")
    private String roleName;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述")
    @Size(max = 200, message = "角色描述长度不能超过 200 个字符")
    private String description;

    /**
     * 状态（0-禁用，1-启用）
     */
    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

}
