package org.dam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dam.entity.UserAuth;

/**
 * 用户认证 Mapper 接口
 * 提供按认证类型 + 登录标识查询认证记录（用于登录校验）
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Mapper
public interface UserAuthMapper extends BaseMapper<UserAuth> {

    /**
     * 根据认证类型和登录标识查询启用状态的认证记录
     * 用于登录时定位用户凭证
     *
     * @param authType   认证类型（1-密码）
     * @param identifier 登录标识（用户名/手机号/邮箱）
     * @return 认证记录，不存在返回 null
     */
    UserAuth selectByAuthTypeAndIdentifier(@Param("authType") Integer authType,
                                            @Param("identifier") String identifier);

}
