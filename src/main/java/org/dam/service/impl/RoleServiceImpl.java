package org.dam.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.dam.common.enums.ResultCode;
import org.dam.common.exception.BizException;
import org.dam.dto.RolePageDTO;
import org.dam.dto.RolePermissionAssignDTO;
import org.dam.dto.RoleSaveDTO;
import org.dam.entity.Permission;
import org.dam.entity.Role;
import org.dam.entity.RolePermission;
import org.dam.mapper.PermissionMapper;
import org.dam.mapper.RoleMapper;
import org.dam.mapper.RolePermissionMapper;
import org.dam.service.RoleService;
import org.dam.vo.PermissionVO;
import org.dam.vo.RolePermissionVO;
import org.dam.vo.RoleVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * 实现角色相关的业务逻辑，包含角色 CRUD、角色分配权限、查询角色权限
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Resource
    private PermissionMapper permissionMapper;

    /**
     * 分页查询角色
     *
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    @Override
    public Page<RoleVO> pageRole(RolePageDTO pageDTO) {
        log.info("分页查询角色，current={}，size={}", pageDTO.getCurrent(), pageDTO.getSize());
        LambdaQueryWrapper<Role> wrapper = Wrappers.lambdaQuery(Role.class)
                .like(StrUtil.isNotBlank(pageDTO.getRoleCode()), Role::getRoleCode, pageDTO.getRoleCode())
                .like(StrUtil.isNotBlank(pageDTO.getRoleName()), Role::getRoleName, pageDTO.getRoleName())
                .eq(Objects.nonNull(pageDTO.getStatus()), Role::getStatus, pageDTO.getStatus())
                .orderByDesc(Role::getId);
        Page<Role> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSize());
        Page<Role> rolePage = roleMapper.selectPage(page, wrapper);
        Page<RoleVO> resultPage = new Page<>(rolePage.getCurrent(), rolePage.getSize(), rolePage.getTotal());
        List<RoleVO> voList = BeanUtil.copyToList(rolePage.getRecords(), RoleVO.class);
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 根据 ID 查询角色详情
     *
     * @param id 角色 ID
     * @return 角色视图对象
     */
    @Override
    public RoleVO getRoleById(Long id) {
        log.info("查询角色详情，id={}", id);
        Role role = roleMapper.selectById(id);
        if (Objects.isNull(role)) {
            throw new BizException(ResultCode.NOT_FOUND, "角色不存在");
        }
        return BeanUtil.copyProperties(role, RoleVO.class);
    }

    /**
     * 新增角色
     *
     * @param saveDTO 角色保存参数
     * @return 新增后的角色 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"rbac:roles", "rbac:perms"}, allEntries = true)
    public Long saveRole(RoleSaveDTO saveDTO) {
        log.info("新增角色，roleCode={}，roleName={}", saveDTO.getRoleCode(), saveDTO.getRoleName());
        checkRoleCodeUnique(saveDTO.getRoleCode(), null);
        Role role = BeanUtil.copyProperties(saveDTO, Role.class);
        if (Objects.isNull(role.getStatus())) {
            role.setStatus(1);
        }
        role.setBuiltIn(0);
        roleMapper.insert(role);
        return role.getId();
    }

    /**
     * 修改角色
     *
     * @param saveDTO 角色保存参数（必须包含 ID）
     * @return 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"rbac:roles", "rbac:perms"}, allEntries = true)
    public Boolean updateRole(RoleSaveDTO saveDTO) {
        if (Objects.isNull(saveDTO.getId())) {
            throw new BizException(ResultCode.PARAM_VALIDATE_FAILED, "角色 ID 不能为空");
        }
        log.info("修改角色，id={}，roleCode={}", saveDTO.getId(), saveDTO.getRoleCode());
        Role existRole = roleMapper.selectById(saveDTO.getId());
        if (Objects.isNull(existRole)) {
            throw new BizException(ResultCode.NOT_FOUND, "角色不存在");
        }
        checkRoleCodeUnique(saveDTO.getRoleCode(), saveDTO.getId());
        // 内置角色的 roleCode 不允许修改
        if (Integer.valueOf(1).equals(existRole.getBuiltIn())
                && !StrUtil.equals(existRole.getRoleCode(), saveDTO.getRoleCode())) {
            throw new BizException(ResultCode.BIZ_ERROR, "内置角色编码不允许修改");
        }
        Role role = BeanUtil.copyProperties(saveDTO, Role.class);
        int rows = roleMapper.updateById(role);
        return rows > 0;
    }

    /**
     * 根据 ID 逻辑删除角色
     * 内置角色（built_in=1）不允许删除
     *
     * @param id 角色 ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"rbac:roles", "rbac:perms"}, allEntries = true)
    public Boolean removeRole(Long id) {
        log.info("删除角色，id={}", id);
        Role role = roleMapper.selectById(id);
        if (Objects.isNull(role)) {
            throw new BizException(ResultCode.NOT_FOUND, "角色不存在");
        }
        if (Integer.valueOf(1).equals(role.getBuiltIn())) {
            throw new BizException(ResultCode.BIZ_ERROR, "内置角色不允许删除");
        }
        // 逻辑删除角色
        int rows = roleMapper.deleteById(id);
        // 同时逻辑删除 sys_role_permission 中该角色的权限关联
        if (rows > 0) {
            LambdaQueryWrapper<RolePermission> rpWrapper = Wrappers.lambdaQuery(RolePermission.class)
                    .eq(RolePermission::getRoleId, id);
            rolePermissionMapper.delete(rpWrapper);
            log.info("级联逻辑删除角色权限关联，roleId={}", id);
        }
        return rows > 0;
    }

    /**
     * 给角色分配权限（全量覆盖）
     * 全量覆盖策略：移除不在新列表中的旧关联，补齐新关联；同事务保证一致性
     *
     * @param assignDTO 角色分配权限参数
     * @return 是否分配成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"rbac:roles", "rbac:perms"}, allEntries = true)
    public Boolean assignPermissions(RolePermissionAssignDTO assignDTO) {
        Long roleId = assignDTO.getRoleId();
        List<Long> permissionIds = assignDTO.getPermissionIds();
        if (Objects.isNull(roleId)) {
            throw new BizException(ResultCode.PARAM_VALIDATE_FAILED, "角色 ID 不能为空");
        }
        if (CollUtil.isEmpty(permissionIds)) {
            throw new BizException(ResultCode.PARAM_VALIDATE_FAILED, "权限 ID 集合不能为空");
        }
        Role role = roleMapper.selectById(roleId);
        if (Objects.isNull(role)) {
            throw new BizException(ResultCode.NOT_FOUND, "角色不存在");
        }
        log.info("给角色分配权限，roleId={}，permissionIds={}", roleId, permissionIds);

        // 1. 查询当前角色已关联的权限 ID 集合
        LambdaQueryWrapper<RolePermission> existWrapper = Wrappers.lambdaQuery(RolePermission.class)
                .eq(RolePermission::getRoleId, roleId);
        List<RolePermission> existList = rolePermissionMapper.selectList(existWrapper);
        List<Long> existPermissionIds = existList.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        // 2. 计算需要删除的差集（旧关联 - 新列表）
        List<Long> toRemoveIds = existPermissionIds.stream()
                .filter(pid -> !permissionIds.contains(pid))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(toRemoveIds)) {
            LambdaQueryWrapper<RolePermission> removeWrapper = Wrappers.lambdaQuery(RolePermission.class)
                    .eq(RolePermission::getRoleId, roleId)
                    .in(RolePermission::getPermissionId, toRemoveIds);
            rolePermissionMapper.delete(removeWrapper);
            log.info("移除角色旧权限关联，roleId={}，removedPermissionIds={}", roleId, toRemoveIds);
        }

        // 3. 计算需要新增的差集（新列表 - 旧关联）
        List<Long> toAddIds = permissionIds.stream()
                .filter(pid -> !existPermissionIds.contains(pid))
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(toAddIds)) {
            String createBy = "system";
            for (Long pid : toAddIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(pid);
                rp.setCreateBy(createBy);
                rolePermissionMapper.insert(rp);
            }
            log.info("新增角色权限关联，roleId={}，addedPermissionIds={}", roleId, toAddIds);
        }
        return Boolean.TRUE;
    }

    /**
     * 查询角色已分配和未分配的权限（用于前端勾选展示）
     *
     * @param roleId 角色 ID
     * @return 角色权限视图对象（包含已分配和未分配两个集合）
     */
    @Override
    public RolePermissionVO listRolePermissions(Long roleId) {
        log.info("查询角色权限列表，roleId={}", roleId);
        Role role = roleMapper.selectById(roleId);
        if (Objects.isNull(role)) {
            throw new BizException(ResultCode.NOT_FOUND, "角色不存在");
        }
        RolePermissionVO vo = new RolePermissionVO();
        vo.setRoleId(roleId);
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());

        List<Permission> assigned = permissionMapper.selectPermissionsByRoleId(roleId);
        List<Permission> unassigned = permissionMapper.selectUnassignedPermissionsByRoleId(roleId);
        vo.setAssignedPermissions(BeanUtil.copyToList(assigned, PermissionVO.class));
        vo.setUnassignedPermissions(BeanUtil.copyToList(unassigned, PermissionVO.class));
        return vo;
    }

    /**
     * 校验角色编码是否唯一
     *
     * @param roleCode   角色编码
     * @param excludeId 排除的 ID（修改时传当前 ID，新增时传 null）
     */
    private void checkRoleCodeUnique(String roleCode, Long excludeId) {
        LambdaQueryWrapper<Role> wrapper = Wrappers.lambdaQuery(Role.class)
                .eq(Role::getRoleCode, roleCode)
                .ne(Objects.nonNull(excludeId), Role::getId, excludeId);
        Long count = roleMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BIZ_ERROR, "角色编码已存在");
        }
    }

}
