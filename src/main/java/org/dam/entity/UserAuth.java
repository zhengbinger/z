package org.dam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户认证实体类
 * 对应数据库表 sys_user_auth，记录用户的认证凭证
 * 一个用户可拥有多条不同 auth_type 的认证记录，支持多种登录方式
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_auth")
@Schema(description = "用户认证信息")
public class UserAuth extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户 ID
     */
    @Schema(description = "关联用户 ID")
    private Long userId;

    /**
     * 认证类型（1-密码，2-手机验证码，3-第三方 OAuth）
     */
    @Schema(description = "认证类型（1-密码，2-手机验证码，3-第三方 OAuth）")
    private Integer authType;

    /**
     * 登录标识（用户名/手机号/邮箱）
     */
    @Schema(description = "登录标识")
    private String identifier;

    /**
     * 凭证（密码认证下为 BCrypt 哈希）
     */
    @Schema(description = "凭证（BCrypt 哈希）")
    private String credential;

    /**
     * 状态（0-禁用，1-启用）
     */
    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;

}
