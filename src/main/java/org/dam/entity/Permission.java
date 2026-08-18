package org.dam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限实体类
 * 对应数据库表 sys_permission
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
@Schema(description = "权限信息")
public class Permission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限编码（程序使用，唯一，如 user:list）
     */
    @Schema(description = "权限编码")
    private String permissionCode;

    /**
     * 权限名称（展示用）
     */
    @Schema(description = "权限名称")
    private String permissionName;

    /**
     * 类型（1-菜单，2-按钮，3-接口）
     */
    @Schema(description = "类型（1-菜单，2-按钮，3-接口）")
    private Integer type;

    /**
     * 父级 ID（0-根节点）
     */
    @Schema(description = "父级 ID")
    private Long parentId;

    /**
     * 访问路径
     */
    @Schema(description = "访问路径")
    private String path;

    /**
     * HTTP 方法（GET/POST/PUT/DELETE）
     */
    @Schema(description = "HTTP 方法")
    private String method;

    /**
     * 权限描述
     */
    @Schema(description = "权限描述")
    private String description;

    /**
     * 排序（数字越小越靠前）
     */
    @Schema(description = "排序")
    private Integer sort;

    /**
     * 状态（0-禁用，1-启用）
     */
    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

}
