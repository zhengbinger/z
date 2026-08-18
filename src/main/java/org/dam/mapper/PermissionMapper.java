package org.dam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dam.entity.Permission;

import java.util.List;

/**
 * 权限 Mapper 接口
 * 提供按用户 ID / 角色 ID 查询权限的关联查询（JOIN sys_role_permission / sys_user_role）
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户 ID 查询该用户拥有的全部权限（启用状态、未逻辑删除）
     * 关联路径：sys_user_role → sys_role_permission → sys_permission
     *
     * @param userId 用户 ID
     * @return 权限列表，无关联返回空集合
     */
    List<Permission> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据角色 ID 查询该角色关联的全部权限（启用状态、未逻辑删除）
     *
     * @param roleId 角色 ID
     * @return 权限列表，无关联返回空集合
     */
    List<Permission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色 ID 查询未关联的权限列表（用于"未分配权限"展示）
     *
     * @param roleId 角色 ID
     * @return 权限列表
     */
    List<Permission> selectUnassignedPermissionsByRoleId(@Param("roleId") Long roleId);

}
