package org.dam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dam.entity.UserRole;

/**
 * 用户-角色关联 Mapper 接口
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
