package org.dam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dam.dto.PermissionPageDTO;
import org.dam.dto.PermissionSaveDTO;
import org.dam.vo.PermissionVO;

/**
 * 权限服务接口
 * 定义权限相关的业务操作，包含权限 CRUD
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
public interface PermissionService {

    /**
     * 分页查询权限
     *
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    Page<PermissionVO> pagePermission(PermissionPageDTO pageDTO);

    /**
     * 根据 ID 查询权限详情
     *
     * @param id 权限 ID
     * @return 权限视图对象
     */
    PermissionVO getPermissionById(Long id);

    /**
     * 新增权限
     *
     * @param saveDTO 权限保存参数
     * @return 新增后的权限 ID
     */
    Long savePermission(PermissionSaveDTO saveDTO);

    /**
     * 修改权限
     *
     * @param saveDTO 权限保存参数（必须包含 ID）
     * @return 是否修改成功
     */
    Boolean updatePermission(PermissionSaveDTO saveDTO);

    /**
     * 根据 ID 逻辑删除权限
     * 若权限已被角色关联，需先解绑，否则会因外键约束（业务层校验）拒绝删除
     *
     * @param id 权限 ID
     * @return 是否删除成功
     */
    Boolean removePermission(Long id);

}
