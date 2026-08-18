package org.dam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 用户分页查询 DTO
 * 用于接收前端分页查询请求
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "用户分页查询请求")
public class UserPageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码（从 1 开始）
     */
    @Schema(description = "当前页码", required = true, example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于 0")
    private Long current;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", required = true, example = "10")
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数必须大于 0")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Long size;

    /**
     * 用户名（模糊查询）
     */
    @Schema(description = "用户名（模糊查询）")
    private String username;

    /**
     * 手机号（模糊查询）
     */
    @Schema(description = "手机号（模糊查询）")
    private String phone;

    /**
     * 状态（0-禁用，1-启用）
     */
    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

}
