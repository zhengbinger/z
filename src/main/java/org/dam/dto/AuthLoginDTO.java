package org.dam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录请求 DTO
 * 用户提交登录标识（用户名/手机号/邮箱）+ 凭证（密码）
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "登录请求")
public class AuthLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 登录标识（用户名/手机号/邮箱）
     */
    @Schema(description = "登录标识", required = true, example = "admin")
    @NotBlank(message = "登录标识不能为空")
    private String identifier;

    /**
     * 凭证（密码明文，服务端用 BCrypt 校验）
     */
    @Schema(description = "凭证（密码）", required = true, example = "admin123")
    @NotBlank(message = "凭证不能为空")
    private String credential;

    /**
     * 认证类型（1-密码，默认 1；预留扩展手机验证码、第三方 OAuth）
     */
    @Schema(description = "认证类型（1-密码，默认 1）")
    private Integer authType;

}
