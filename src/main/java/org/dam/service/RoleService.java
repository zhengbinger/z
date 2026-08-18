package org.dam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dam.dto.RolePageDTO;
import org.dam.dto.RolePermissionAssignDTO;
import org.dam.dto.RoleSaveDTO;
import org.dam.vo.RolePermissionVO;
import org.dam.vo.RoleVO;

/**
 * 角色服务接口
 * 定义角色相关的业务操作，包含角色 CRUD、角色分配权限、查询角色权限
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
public interface RoleService {

    /**
     * 分页查询角色
     *
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    Page<RoleVO> pageRole(RolePageDTO pageDTO);

    /**
     * 根据 ID 查询角色详情
     *
     * @param id 角色 ID
     * @return 角色视图对象
     */
    RoleVO getRoleById(Long id);

    /**
     * 新增角色
     *
     * @param saveDTO 角色保存参数
     * @return 新增后的角色 ID
     */
    Long saveRole(RoleSaveDTO saveDTO);

    /**
     * 修改角色
     *
     * @param saveDTO 角色保存参数（必须包含 ID）
     * @return 是否修改成功
     */
    Boolean updateRole(RoleSaveDTO saveDTO);

    /**
     * 根据 ID 逻辑删除角色
     * 内置角色（built_in=1）不允许删除
     *
     * @param id 角色 ID
     * @return 是否删除成功
     */
    Boolean removeRole(Long id);

    /**
     * 给角色分配权限（全量覆盖）
     * 传入的 permissionIds 即为该角色的最终权限集合，
     * 移除不在列表中的旧关联，补齐新关联
     *
     * @param assignDTO 角色分配权限参数
     * @return 是否分配成功
     */
    Boolean assignPermissions(RolePermissionAssignDTO assignDTO);

    /**
     * 查询角色已分配和未分配的权限（用于前端勾选展示）
     *
     * @param roleId 角色 ID
     * @return 角色权限视图对象（包含已分配和未分配两个集合）
     */
    RolePermissionVO listRolePermissions(Long roleId);

}
