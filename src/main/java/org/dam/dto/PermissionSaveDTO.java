package org.dam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 权限新增/修改 DTO
 * 用于接收前端提交的权限表单数据
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "权限新增/修改请求")
public class PermissionSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID（新增时不传，修改时必传）
     */
    @Schema(description = "主键 ID（修改时必传）")
    private Long id;

    /**
     * 权限编码（程序使用，唯一，如 user:list）
     */
    @Schema(description = "权限编码", required = true)
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过 100 个字符")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_:]*$", message = "权限编码需以字母开头，仅含字母、数字、下划线、冒号")
    private String permissionCode;

    /**
     * 权限名称（展示用）
     */
    @Schema(description = "权限名称", required = true)
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过 50 个字符")
    private String permissionName;

    /**
     * 类型（1-菜单，2-按钮，3-接口）
     */
    @Schema(description = "类型（1-菜单，2-按钮，3-接口）", required = true)
    @NotNull(message = "权限类型不能为空")
    private Integer type;

    /**
     * 父级 ID（0-根节点）
     */
    @Schema(description = "父级 ID（0-根节点）")
    private Long parentId;

    /**
     * 访问路径
     */
    @Schema(description = "访问路径")
    @Size(max = 200, message = "访问路径长度不能超过 200 个字符")
    private String path;

    /**
     * HTTP 方法（GET/POST/PUT/DELETE）
     */
    @Schema(description = "HTTP 方法（GET/POST/PUT/DELETE）")
    private String method;

    /**
     * 权限描述
     */
    @Schema(description = "权限描述")
    @Size(max = 200, message = "权限描述长度不能超过 200 个字符")
    private String description;

    /**
     * 排序（数字越小越靠前）
     */
    @Schema(description = "排序（数字越小越靠前）")
    private Integer sort;

    /**
     * 状态（0-禁用，1-启用）
     */
    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

}
