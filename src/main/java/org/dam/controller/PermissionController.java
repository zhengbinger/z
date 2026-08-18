package org.dam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.dam.common.response.Result;
import org.dam.component.security.annotation.RequiresPermission;
import org.dam.dto.PermissionPageDTO;
import org.dam.dto.PermissionSaveDTO;
import org.dam.service.PermissionService;
import org.dam.vo.PermissionVO;
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
 * 权限管理 Controller
 * 提供权限的分页查询、详情、新增、修改、删除接口
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@RestController
@RequestMapping("/permission")
@Tag(name = "权限管理", description = "权限的增删改查接口")
public class PermissionController {

    @Resource
    private PermissionService permissionService;

    /**
     * 分页查询权限
     *
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询权限", description = "支持按权限编码、权限名称、类型、状态过滤")
    @RequiresPermission("permission:list")
    public Result<Page<PermissionVO>> page(@Valid @RequestBody PermissionPageDTO pageDTO) {
        Page<PermissionVO> page = permissionService.pagePermission(pageDTO);
        return Result.success(page);
    }

    /**
     * 根据 ID 查询权限详情
     *
     * @param id 权限 ID
     * @return 权限详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询权限详情")
    @RequiresPermission("permission:list")
    public Result<PermissionVO> get(@Parameter(description = "权限 ID", required = true) @PathVariable Long id) {
        PermissionVO vo = permissionService.getPermissionById(id);
        return Result.success(vo);
    }

    /**
     * 新增权限
     *
     * @param saveDTO 权限保存参数
     * @return 新增后的权限 ID
     */
    @PostMapping
    @Operation(summary = "新增权限")
    @RequiresPermission("permission:add")
    public Result<Long> save(@Valid @RequestBody PermissionSaveDTO saveDTO) {
        Long id = permissionService.savePermission(saveDTO);
        return Result.success(id);
    }

    /**
     * 修改权限
     *
     * @param saveDTO 权限保存参数（必须包含 ID）
     * @return 是否修改成功
     */
    @PutMapping
    @Operation(summary = "修改权限")
    @RequiresPermission("permission:update")
    public Result<Boolean> update(@Valid @RequestBody PermissionSaveDTO saveDTO) {
        Boolean success = permissionService.updatePermission(saveDTO);
        return Result.success(success);
    }

    /**
     * 根据 ID 删除权限（逻辑删除）
     * 会级联清理 sys_role_permission 中的关联记录
     *
     * @param id 权限 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限", description = "会级联清理角色权限关联")
    @RequiresPermission("permission:delete")
    public Result<Boolean> remove(@Parameter(description = "权限 ID", required = true) @PathVariable Long id) {
        Boolean success = permissionService.removePermission(id);
        return Result.success(success);
    }

}
