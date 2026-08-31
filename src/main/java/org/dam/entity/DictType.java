package org.dam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型实体
 * 对应数据库表 sys_dict_type
 * 一个 dictCode 对应一种分类（如 user_status）
 *
 * @author zhengbing
 * @since 2026-08-31
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
@Schema(description = "字典类型")
public class DictType extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字典编码（程序使用，唯一，如 user_status）
     */
    @Schema(description = "字典编码")
    private String dictCode;

    /**
     * 字典名称-中文
     */
    @Schema(description = "字典名称-中文")
    private String dictNameZh;

    /**
     * 字典名称-英文
     */
    @Schema(description = "字典名称-英文")
    private String dictNameEn;

    /**
     * 状态（0-禁用，1-启用）
     */
    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
