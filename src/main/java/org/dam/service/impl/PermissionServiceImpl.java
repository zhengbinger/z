package org.dam.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.dam.common.enums.ResultCode;
import org.dam.common.exception.BizException;
import org.dam.dto.PermissionPageDTO;
import org.dam.dto.PermissionSaveDTO;
import org.dam.entity.Permission;
import org.dam.entity.RolePermission;
import org.dam.mapper.PermissionMapper;
import org.dam.mapper.RolePermissionMapper;
import org.dam.service.PermissionService;
import org.dam.vo.PermissionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 权限服务实现类
 * 实现权限相关的业务逻辑，包含权限 CRUD
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    /**
     * 分页查询权限
     *
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    @Override
    public Page<PermissionVO> pagePermission(PermissionPageDTO pageDTO) {
        log.info("分页查询权限，current={}，size={}", pageDTO.getCurrent(), pageDTO.getSize());
        LambdaQueryWrapper<Permission> wrapper = Wrappers.lambdaQuery(Permission.class)
                .like(StrUtil.isNotBlank(pageDTO.getPermissionCode()), Permission::getPermissionCode, pageDTO.getPermissionCode())
                .like(StrUtil.isNotBlank(pageDTO.getPermissionName()), Permission::getPermissionName, pageDTO.getPermissionName())
                .eq(Objects.nonNull(pageDTO.getType()), Permission::getType, pageDTO.getType())
                .eq(Objects.nonNull(pageDTO.getStatus()), Permission::getStatus, pageDTO.getStatus())
                .orderByAsc(Permission::getSort)
                .orderByDesc(Permission::getId);
        Page<Permission> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSize());
        Page<Permission> permissionPage = permissionMapper.selectPage(page, wrapper);
        Page<PermissionVO> resultPage = new Page<>(permissionPage.getCurrent(), permissionPage.getSize(), permissionPage.getTotal());
        List<PermissionVO> voList = BeanUtil.copyToList(permissionPage.getRecords(), PermissionVO.class);
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 根据 ID 查询权限详情
     *
     * @param id 权限 ID
     * @return 权限视图对象
     */
    @Override
    public PermissionVO getPermissionById(Long id) {
        log.info("查询权限详情，id={}", id);
        Permission permission = permissionMapper.selectById(id);
        if (Objects.isNull(permission)) {
            throw new BizException(ResultCode.NOT_FOUND, "权限不存在");
        }
        return BeanUtil.copyProperties(permission, PermissionVO.class);
    }

    /**
     * 新增权限
     *
     * @param saveDTO 权限保存参数
     * @return 新增后的权限 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long savePermission(PermissionSaveDTO saveDTO) {
        log.info("新增权限，permissionCode={}，permissionName={}", saveDTO.getPermissionCode(), saveDTO.getPermissionName());
        checkPermissionCodeUnique(saveDTO.getPermissionCode(), null);
        Permission permission = BeanUtil.copyProperties(saveDTO, Permission.class);
        if (Objects.isNull(permission.getParentId())) {
            permission.setParentId(0L);
        }
        if (Objects.isNull(permission.getSort())) {
            permission.setSort(0);
        }
        if (Objects.isNull(permission.getStatus())) {
            permission.setStatus(1);
        }
        permissionMapper.insert(permission);
        return permission.getId();
    }

    /**
     * 修改权限
     *
     * @param saveDTO 权限保存参数（必须包含 ID）
     * @return 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePermission(PermissionSaveDTO saveDTO) {
        if (Objects.isNull(saveDTO.getId())) {
            throw new BizException(ResultCode.PARAM_VALIDATE_FAILED, "权限 ID 不能为空");
        }
        log.info("修改权限，id={}，permissionCode={}", saveDTO.getId(), saveDTO.getPermissionCode());
        Permission exist = permissionMapper.selectById(saveDTO.getId());
        if (Objects.isNull(exist)) {
            throw new BizException(ResultCode.NOT_FOUND, "权限不存在");
        }
        checkPermissionCodeUnique(saveDTO.getPermissionCode(), saveDTO.getId());
        Permission permission = BeanUtil.copyProperties(saveDTO, Permission.class);
        int rows = permissionMapper.updateById(permission);
        return rows > 0;
    }

    /**
     * 根据 ID 逻辑删除权限
     * 若权限已被角色关联，会级联逻辑删除 sys_role_permission 中的关联记录
     *
     * @param id 权限 ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removePermission(Long id) {
        log.info("删除权限，id={}", id);
        Permission permission = permissionMapper.selectById(id);
        if (Objects.isNull(permission)) {
            throw new BizException(ResultCode.NOT_FOUND, "权限不存在");
        }
        int rows = permissionMapper.deleteById(id);
        if (rows > 0) {
            // 级联逻辑删除 sys_role_permission 中该权限的关联记录
            LambdaQueryWrapper<RolePermission> rpWrapper = Wrappers.lambdaQuery(RolePermission.class)
                    .eq(RolePermission::getPermissionId, id);
            rolePermissionMapper.delete(rpWrapper);
            log.info("级联逻辑删除角色权限关联，permissionId={}", id);
        }
        return rows > 0;
    }

    /**
     * 校验权限编码是否唯一
     *
     * @param permissionCode 权限编码
     * @param excludeId      排除的 ID（修改时传当前 ID，新增时传 null）
     */
    private void checkPermissionCodeUnique(String permissionCode, Long excludeId) {
        LambdaQueryWrapper<Permission> wrapper = Wrappers.lambdaQuery(Permission.class)
                .eq(Permission::getPermissionCode, permissionCode)
                .ne(Objects.nonNull(excludeId), Permission::getId, excludeId);
        Long count = permissionMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BIZ_ERROR, "权限编码已存在");
        }
    }

}
