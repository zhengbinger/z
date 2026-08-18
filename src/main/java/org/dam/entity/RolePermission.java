package org.dam.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色-权限关联实体类
 * 对应数据库表 sys_role_permission
 * 多对多关联表，无 update 字段，只有 create + 逻辑删除
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@TableName("sys_role_permission")
@Schema(description = "角色-权限关联")
public class RolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色 ID
     */
    @Schema(description = "角色 ID")
    private Long roleId;

    /**
     * 权限 ID
     */
    @Schema(description = "权限 ID")
    private Long permissionId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @Schema(description = "逻辑删除标识")
    @TableField("deleted")
    private Integer deleted;

}
