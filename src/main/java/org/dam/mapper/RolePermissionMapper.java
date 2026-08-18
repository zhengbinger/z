package org.dam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dam.entity.RolePermission;

/**
 * 角色-权限关联 Mapper 接口
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}
