package org.dam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典项实体
 * 对应数据库表 sys_dict_data
 * 一个 dict_value 对应一个选项（如 0-禁用）
 *
 * @author zhengbing
 * @since 2026-08-31
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
@Schema(description = "字典项")
public class DictData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型 ID
     */
    @Schema(description = "字典类型 ID")
    private Long dictTypeId;

    /**
     * 字典标签-中文（展示用，如：禁用）
     */
    @Schema(description = "字典标签-中文")
    private String dictLabelZh;

    /**
     * 字典标签-英文（如：Disabled）
     */
    @Schema(description = "字典标签-英文")
    private String dictLabelEn;

    /**
     * 字典值（程序使用，如 0）
     */
    @Schema(description = "字典值")
    private String dictValue;

    /**
     * 前端样式类（如 danger，便于按值着色）
     */
    @Schema(description = "前端样式类")
    private String cssClass;

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

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
