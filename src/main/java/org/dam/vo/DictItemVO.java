package org.dam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典项 VO
 * 按当前 Locale 输出 label，前端只关心"值 + 当前语言标签 + 样式类"
 *
 * @author zhengbing
 * @since 2026-08-31
 **/
@Data
@Schema(description = "字典项")
public class DictItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典值
     */
    @Schema(description = "字典值")
    private String value;

    /**
     * 字典标签（按当前 Locale）
     */
    @Schema(description = "字典标签（按当前 Locale）")
    private String label;

    /**
     * 前端样式类
     */
    @Schema(description = "前端样式类")
    private String cssClass;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sort;

}
