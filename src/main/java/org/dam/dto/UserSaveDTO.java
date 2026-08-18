package org.dam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户新增/修改 DTO
 * 用于接收前端提交的用户表单数据
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "用户新增/修改请求")
public class UserSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID（新增时不传，修改时必传）
     */
    @Schema(description = "主键 ID（修改时必传）")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度需在 3-30 个字符之间")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    @Size(max = 50, message = "昵称长度不能超过 50 个字符")
    private String nickname;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100 个字符")
    private String email;

    /**
     * 性别（0-未知，1-男，2-女）
     */
    @Schema(description = "性别（0-未知，1-男，2-女）")
    private Integer gender;

    /**
     * 状态（0-禁用，1-启用）
     */
    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

}
