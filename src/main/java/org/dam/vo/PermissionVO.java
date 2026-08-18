package org.dam.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限视图对象
 * 用于向前端返回权限信息
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "权限信息响应")
public class PermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;

    /**
     * 权限编码
     */
    @Schema(description = "权限编码")
    private String permissionCode;

    /**
     * 权限名称
     */
    @Schema(description = "权限名称")
    private String permissionName;

    /**
     * 类型（1-菜单，2-按钮，3-接口）
     */
    @Schema(description = "类型（1-菜单，2-按钮，3-接口）")
    private Integer type;

    /**
     * 父级 ID
     */
    @Schema(description = "父级 ID")
    private Long parentId;

    /**
     * 访问路径
     */
    @Schema(description = "访问路径")
    private String path;

    /**
     * HTTP 方法
     */
    @Schema(description = "HTTP 方法")
    private String method;

    /**
     * 权限描述
     */
    @Schema(description = "权限描述")
    private String description;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sort;

    /**
     * 状态（0-禁用，1-启用）
     */
    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;

}
