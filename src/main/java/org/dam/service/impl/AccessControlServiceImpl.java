package org.dam.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.dam.entity.Permission;
import org.dam.entity.Role;
import org.dam.mapper.PermissionMapper;
import org.dam.mapper.RoleMapper;
import org.dam.service.AccessControlService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 访问控制服务实现
 * 角色/权限编码集合走 Spring Cache（@Cacheable，底层 Redis），TTL 30 分钟
 * 缓存粒度：按 userId 缓存"角色编码集合"和"权限编码集合"两个列表
 * 权限/角色变更时由 RoleService/PermissionService/UserService 主动 @CacheEvict 清除
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
    @Cacheable(value = "rbac:roles", key = "#userId")
    public List<String> listRoleCodesByUserId(Long userId) {
        if (Objects.isNull(userId)) {
            return new ArrayList<>();
        }
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        if (CollUtil.isEmpty(roles)) {
            return new ArrayList<>();
        }
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
    @Cacheable(value = "rbac:perms", key = "#userId")
    public List<String> listPermissionCodesByUserId(Long userId) {
        if (Objects.isNull(userId)) {
            return new ArrayList<>();
        }
        List<Permission> permissions = permissionMapper.selectPermissionsByUserId(userId);
        if (CollUtil.isEmpty(permissions)) {
            return new ArrayList<>();
        }
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
        // 走 @Cacheable 缓存
        List<String> codes = listPermissionCodesByUserId(userId);
        return codes.contains(permissionCode);
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        if (Objects.isNull(userId) || StrUtil.isBlank(roleCode)) {
            return false;
        }
        // 走 @Cacheable 缓存
        List<String> codes = listRoleCodesByUserId(userId);
        return codes.contains(roleCode);
    }

    @Override
    public boolean hasAnyRole(Long userId, String... roleCodes) {
        if (Objects.isNull(userId) || roleCodes == null || roleCodes.length == 0) {
            return false;
        }
        // 走 @Cacheable 缓存
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
        // 走 @Cacheable 缓存
        List<String> userPerms = listPermissionCodesByUserId(userId);
        for (String code : permissionCodes) {
            if (StrUtil.isNotBlank(code) && userPerms.contains(code)) {
                return true;
            }
        }
        return false;
    }

}
