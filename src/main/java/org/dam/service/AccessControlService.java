package org.dam.service;

import org.dam.entity.Permission;
import org.dam.entity.Role;

import java.util.List;

/**
 * 访问控制服务接口
 * 封装"用户 - 角色 - 权限"的关联查询，供 RBAC 切面与业务层调用
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
public interface AccessControlService {

    /**
     * 查询用户拥有的全部角色编码
     *
     * @param userId 用户 ID
     * @return 角色编码集合，无关联返回空集合
     */
    List<String> listRoleCodesByUserId(Long userId);

    /**
     * 查询用户拥有的全部角色
     *
     * @param userId 用户 ID
     * @return 角色列表，无关联返回空集合
     */
    List<Role> listRolesByUserId(Long userId);

    /**
     * 查询用户拥有的全部权限编码
     *
     * @param userId 用户 ID
     * @return 权限编码集合，无关联返回空集合
     */
    List<String> listPermissionCodesByUserId(Long userId);

    /**
     * 查询用户拥有的全部权限
     *
     * @param userId 用户 ID
     * @return 权限列表，无关联返回空集合
     */
    List<Permission> listPermissionsByUserId(Long userId);

    /**
     * 校验用户是否拥有指定权限编码
     *
     * @param userId         用户 ID
     * @param permissionCode 权限编码
     * @return true 表示有权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 校验用户是否拥有指定角色编码
     *
     * @param userId   用户 ID
     * @param roleCode 角色编码
     * @return true 表示有角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 校验用户是否拥有任意一个角色编码
     *
     * @param userId    用户 ID
     * @param roleCodes 角色编码数组
     * @return true 表示至少匹配一个
     */
    boolean hasAnyRole(Long userId, String... roleCodes);

    /**
     * 校验用户是否拥有任意一个权限编码
     *
     * @param userId          用户 ID
     * @param permissionCodes 权限编码数组
     * @return true 表示至少匹配一个
     */
    boolean hasAnyPermission(Long userId, String... permissionCodes);

}
