package org.dam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.dam.entity.Role;

import java.util.List;

/**
 * 角色 Mapper 接口
 * 提供按用户 ID 查询角色的关联查询（JOIN sys_user_role）
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户 ID 查询该用户关联的全部角色（启用状态、未逻辑删除）
     *
     * @param userId 用户 ID
     * @return 角色列表，无关联返回空集合
     */
    List<Role> selectRolesByUserId(@Param("userId") Long userId);

}
