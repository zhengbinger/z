package org.dam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.dam.common.response.Result;
import org.dam.component.security.annotation.RequiresPermission;
import org.dam.dto.RolePageDTO;
import org.dam.dto.RolePermissionAssignDTO;
import org.dam.dto.RoleSaveDTO;
import org.dam.service.RoleService;
import org.dam.vo.RolePermissionVO;
import org.dam.vo.RoleVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 角色管理 Controller
 * 提供角色的分页查询、详情、新增、修改、删除、分配权限接口
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@RestController
@RequestMapping("/role")
@Tag(name = "角色管理", description = "角色的增删改查及权限分配接口")
public class RoleController {

    @Resource
    private RoleService roleService;

    /**
     * 分页查询角色
     *
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询角色", description = "支持按角色编码、角色名称、状态过滤")
    @RequiresPermission("role:list")
    public Result<Page<RoleVO>> page(@Valid @RequestBody RolePageDTO pageDTO) {
        Page<RoleVO> page = roleService.pageRole(pageDTO);
        return Result.success(page);
    }

    /**
     * 根据 ID 查询角色详情
     *
     * @param id 角色 ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询角色详情")
    @RequiresPermission("role:get")
    public Result<RoleVO> get(@Parameter(description = "角色 ID", required = true) @PathVariable Long id) {
        RoleVO vo = roleService.getRoleById(id);
        return Result.success(vo);
    }

    /**
     * 新增角色
     *
     * @param saveDTO 角色保存参数
     * @return 新增后的角色 ID
     */
    @PostMapping
    @Operation(summary = "新增角色")
    @RequiresPermission("role:add")
    public Result<Long> save(@Valid @RequestBody RoleSaveDTO saveDTO) {
        Long id = roleService.saveRole(saveDTO);
        return Result.success(id);
    }

    /**
     * 修改角色
     *
     * @param saveDTO 角色保存参数（必须包含 ID）
     * @return 是否修改成功
     */
    @PutMapping
    @Operation(summary = "修改角色")
    @RequiresPermission("role:update")
    public Result<Boolean> update(@Valid @RequestBody RoleSaveDTO saveDTO) {
        Boolean success = roleService.updateRole(saveDTO);
        return Result.success(success);
    }

    /**
     * 根据 ID 删除角色（逻辑删除）
     * 内置角色不允许删除
     *
     * @param id 角色 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "内置角色不允许删除，会级联清理角色权限关联")
    @RequiresPermission("role:delete")
    public Result<Boolean> remove(@Parameter(description = "角色 ID", required = true) @PathVariable Long id) {
        Boolean success = roleService.removeRole(id);
        return Result.success(success);
    }

    /**
     * 给角色分配权限（全量覆盖）
     *
     * @param assignDTO 角色分配权限参数
     * @return 是否分配成功
     */
    @PutMapping("/permissions")
    @Operation(summary = "给角色分配权限", description = "全量覆盖：传入的 permissionIds 即为该角色的最终权限集合")
    @RequiresPermission("role:assignPermission")
    public Result<Boolean> assignPermissions(@Valid @RequestBody RolePermissionAssignDTO assignDTO) {
        Boolean success = roleService.assignPermissions(assignDTO);
        return Result.success(success);
    }

    /**
     * 查询角色已分配和未分配的权限（用于前端勾选展示）
     *
     * @param id 角色 ID
     * @return 角色权限视图对象
     */
    @GetMapping("/{id}/permissions")
    @Operation(summary = "查询角色权限列表", description = "返回已分配和未分配两个权限集合，便于前端勾选展示")
    @RequiresPermission("role:get")
    public Result<RolePermissionVO> listPermissions(@Parameter(description = "角色 ID", required = true) @PathVariable Long id) {
        RolePermissionVO vo = roleService.listRolePermissions(id);
        return Result.success(vo);
    }

}
