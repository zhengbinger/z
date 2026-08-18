package org.dam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.dam.common.enums.ResultCode;
import org.dam.common.response.Result;
import org.dam.component.status.UserStatus;
import org.dam.dto.UserPageDTO;
import org.dam.dto.UserSaveDTO;
import org.dam.service.UserService;
import org.dam.vo.UserVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 用户管理 Controller
 * 提供用户的分页查询、详情、新增、修改、删除接口
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户的增删改查接口")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 分页查询用户
     *
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询用户", description = "支持按用户名、手机号、状态过滤")
    public Result<Page<UserVO>> page(@Valid @RequestBody UserPageDTO pageDTO) {
        Page<UserVO> page = userService.pageUser(pageDTO);
        return Result.success(page);
    }

    /**
     * 根据 ID 查询用户详情
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询用户详情")
    public Result<UserVO> get(@Parameter(description = "用户 ID", required = true) @PathVariable Long id) {
        UserVO vo = userService.getUserById(id);
        return Result.success(vo);
    }

    /**
     * 新增用户
     *
     * @param saveDTO 用户保存参数
     * @return 新增后的用户 ID
     */
    @PostMapping
    @Operation(summary = "新增用户")
    public Result<Long> save(@Valid @RequestBody UserSaveDTO saveDTO) {
        Long id = userService.saveUser(saveDTO);
        return Result.success(id);
    }

    /**
     * 修改用户
     *
     * @param saveDTO 用户保存参数（必须包含 ID）
     * @return 是否修改成功
     */
    @PutMapping
    @Operation(summary = "修改用户")
    public Result<Boolean> update(@Valid @RequestBody UserSaveDTO saveDTO) {
        Boolean success = userService.updateUser(saveDTO);
        return Result.success(success);
    }

    /**
     * 根据 ID 删除用户（逻辑删除）
     *
     * @param id 用户 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Boolean> remove(@Parameter(description = "用户 ID", required = true) @PathVariable Long id) {
        Boolean success = userService.removeUser(id);
        return Result.success(success);
    }

    /**
     * 变更用户状态
     * 状态变化时将通过观察者模式触发对应状态处理器的逻辑
     *
     * @param id     用户 ID
     * @param status 目标状态码（0-禁用，1-启用，2-锁定，3-待审核）
     * @return 是否变更成功（状态未变化返回 false）
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "变更用户状态", description = "观察者模式演示：状态变化触发对应处理器执行后续逻辑")
    public Result<Boolean> changeStatus(
            @Parameter(description = "用户 ID", required = true) @PathVariable Long id,
            @Parameter(description = "目标状态码（0-禁用，1-启用，2-锁定，3-待审核）", required = true)
            @RequestParam("status") Integer status) {
        UserStatus targetStatus = UserStatus.ofCode(status);
        if (targetStatus == null) {
            return Result.failed(ResultCode.PARAM_VALIDATE_FAILED.getCode(),
                    "非法状态码，合法值：0-禁用，1-启用，2-锁定，3-待审核");
        }
        Boolean success = userService.changeUserStatus(id, targetStatus);
        return Result.success(success);
    }

}
