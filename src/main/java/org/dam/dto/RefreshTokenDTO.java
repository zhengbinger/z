package org.dam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 刷新 Token 请求 DTO
 * 前端在 access_token 过期后，用 refresh_token 调用刷新接口换取新的 access_token
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "刷新 Token 请求")
public class RefreshTokenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * refresh_token
     */
    @Schema(description = "refresh_token", required = true)
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;

}
