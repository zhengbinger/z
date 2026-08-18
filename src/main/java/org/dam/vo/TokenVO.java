package org.dam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * Token 响应 VO
 * 登录/刷新成功后返回给前端的双 Token 信息
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@Schema(description = "Token 响应")
public class TokenVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * access_token（业务接口鉴权用，短时效）
     */
    @Schema(description = "access_token")
    private String accessToken;

    /**
     * refresh_token（仅用于刷新接口，长时效）
     */
    @Schema(description = "refresh_token")
    private String refreshToken;

    /**
     * Token 类型（固定 Bearer）
     */
    @Schema(description = "Token 类型", example = "Bearer")
    private String tokenType;

    /**
     * access_token 过期秒数（前端据此判断何时主动刷新）
     */
    @Schema(description = "access_token 过期秒数")
    private Long expiresIn;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

}
