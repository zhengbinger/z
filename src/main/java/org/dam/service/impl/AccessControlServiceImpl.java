package org.dam.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.dam.entity.Permission;
import org.dam.entity.Role;
import org.dam.mapper.PermissionMapper;
import org.dam.mapper.RoleMapper;
import org.dam.service.AccessControlService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 访问控制服务实现
 * 直接走 Mapper 关联查询，未做缓存
 * 生产环境建议加 Redis 缓存（key: rbac:user:roles:{userId} / rbac:user:perms:{userId}）
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Service
public class AccessControlServiceImpl implements AccessControlService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Override
    public List<String> listRoleCodesByUserId(Long userId) {
        List<Role> roles = listRolesByUserId(userId);
        return roles.stream()
                .map(Role::getRoleCode)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    @Override
    public List<Role> listRolesByUserId(Long userId) {
        if (Objects.isNull(userId)) {
            return CollUtil.newArrayList();
        }
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        return CollUtil.isNotEmpty(roles) ? roles : CollUtil.newArrayList();
    }

    @Override
    public List<String> listPermissionCodesByUserId(Long userId) {
        List<Permission> permissions = listPermissionsByUserId(userId);
        return permissions.stream()
                .map(Permission::getPermissionCode)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> listPermissionsByUserId(Long userId) {
        if (Objects.isNull(userId)) {
            return CollUtil.newArrayList();
        }
        List<Permission> permissions = permissionMapper.selectPermissionsByUserId(userId);
        return CollUtil.isNotEmpty(permissions) ? permissions : CollUtil.newArrayList();
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (Objects.isNull(userId) || StrUtil.isBlank(permissionCode)) {
            return false;
        }
        List<String> codes = listPermissionCodesByUserId(userId);
        return codes.contains(permissionCode);
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        if (Objects.isNull(userId) || StrUtil.isBlank(roleCode)) {
            return false;
        }
        List<String> codes = listRoleCodesByUserId(userId);
        return codes.contains(roleCode);
    }

    @Override
    public boolean hasAnyRole(Long userId, String... roleCodes) {
        if (Objects.isNull(userId) || roleCodes == null || roleCodes.length == 0) {
            return false;
        }
        List<String> userRoles = listRoleCodesByUserId(userId);
        for (String code : roleCodes) {
            if (StrUtil.isNotBlank(code) && userRoles.contains(code)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAnyPermission(Long userId, String... permissionCodes) {
        if (Objects.isNull(userId) || permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }
        List<String> userPerms = listPermissionCodesByUserId(userId);
        for (String code : permissionCodes) {
            if (StrUtil.isNotBlank(code) && userPerms.contains(code)) {
                return true;
            }
        }
        return false;
    }

}
