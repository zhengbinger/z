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
import org.dam.component.status.UserStatus;
import org.dam.component.status.UserStatusChangeEvent;
import org.dam.component.status.UserStatusChangePublisher;
import org.dam.dto.UserPageDTO;
import org.dam.dto.UserRoleAssignDTO;
import org.dam.dto.UserSaveDTO;
import org.dam.entity.Role;
import org.dam.entity.User;
import org.dam.entity.UserRole;
import org.dam.mapper.RoleMapper;
import org.dam.mapper.UserMapper;
import org.dam.mapper.UserRoleMapper;
import org.dam.service.UserService;
import org.dam.vo.UserRoleVO;
import org.dam.vo.UserVO;
import org.dam.vo.RoleVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 实现用户相关的业务逻辑，包含分页查询、详情、新增、修改、删除
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    /**
     * 手机号脱敏正则：保留前 3 位和后 4 位
     */
    private static final String PHONE_MASK_REGEX = "(\\d{3})\\d{4}(\\d{4})";

    /**
     * 邮箱脱敏正则：保留首字符和 @ 后域名
     */
    private static final String EMAIL_MASK_REGEX = "(\\w?\\w)[^@]*(@\\w+\\.\\w+)";

    private static final String PHONE_MASK_REPLACEMENT = "$1****$2";

    private static final String EMAIL_MASK_REPLACEMENT = "$1***$2";

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserStatusChangePublisher userStatusChangePublisher;

    /**
     * 分页查询用户
     *
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    @Override
    public Page<UserVO> pageUser(UserPageDTO pageDTO) {
        log.info("分页查询用户，current={}，size={}", pageDTO.getCurrent(), pageDTO.getSize());
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
                .like(StrUtil.isNotBlank(pageDTO.getUsername()), User::getUsername, pageDTO.getUsername())
                .like(StrUtil.isNotBlank(pageDTO.getPhone()), User::getPhone, pageDTO.getPhone())
                .eq(Objects.nonNull(pageDTO.getStatus()), User::getStatus, pageDTO.getStatus())
                .orderByDesc(User::getId);
        Page<User> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSize());
        Page<User> userPage = userMapper.selectPage(page, wrapper);
        Page<UserVO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> voList = BeanUtil.copyToList(userPage.getRecords(), UserVO.class);
        if (CollUtil.isNotEmpty(voList)) {
            voList.forEach(this::maskSensitiveField);
        }
        resultPage.setRecords(voList);
        return resultPage;
    }

    /**
     * 根据 ID 查询用户详情
     *
     * @param id 用户 ID
     * @return 用户视图对象
     */
    @Override
    public UserVO getUserById(Long id) {
        log.info("查询用户详情，id={}", id);
        User user = userMapper.selectById(id);
        if (Objects.isNull(user)) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
        maskSensitiveField(vo);
        return vo;
    }

    /**
     * 新增用户
     *
     * @param saveDTO 用户保存参数
     * @return 新增后的用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveUser(UserSaveDTO saveDTO) {
        log.info("新增用户，username={}", saveDTO.getUsername());
        checkUsernameUnique(saveDTO.getUsername(), null);
        User user = BeanUtil.copyProperties(saveDTO, User.class);
        userMapper.insert(user);
        return user.getId();
    }

    /**
     * 修改用户
     *
     * @param saveDTO 用户保存参数（必须包含 ID）
     * @return 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUser(UserSaveDTO saveDTO) {
        if (Objects.isNull(saveDTO.getId())) {
            throw new BizException(ResultCode.PARAM_VALIDATE_FAILED, "用户 ID 不能为空");
        }
        log.info("修改用户，id={}，username={}", saveDTO.getId(), saveDTO.getUsername());
        User existUser = userMapper.selectById(saveDTO.getId());
        if (Objects.isNull(existUser)) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        checkUsernameUnique(saveDTO.getUsername(), saveDTO.getId());
        User user = BeanUtil.copyProperties(saveDTO, User.class);
        int rows = userMapper.updateById(user);
        return rows > 0;
    }

    /**
     * 根据 ID 逻辑删除用户
     *
     * @param id 用户 ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"rbac:roles", "rbac:perms"}, key = "#id")
    public Boolean removeUser(Long id) {
        log.info("删除用户，id={}", id);
        User user = userMapper.selectById(id);
        if (Objects.isNull(user)) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        int rows = userMapper.deleteById(id);
        return rows > 0;
    }

    /**
     * 变更用户状态
     * 状态实际变化时，通过观察者模式发布 {@link UserStatusChangeEvent}，
     * 由关注该目标状态的 {@link org.dam.component.status.UserStatusChangeObserver} 实现处理后续逻辑
     *
     * @param id           用户 ID
     * @param targetStatus 目标状态枚举
     * @return 是否变更成功（状态未变化返回 false）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeUserStatus(Long id, UserStatus targetStatus) {
        if (Objects.isNull(id) || Objects.isNull(targetStatus)) {
            throw new BizException(ResultCode.PARAM_VALIDATE_FAILED, "用户 ID 或目标状态不能为空");
        }
        User user = userMapper.selectById(id);
        if (Objects.isNull(user)) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        UserStatus fromStatus = UserStatus.ofCode(user.getStatus());
        if (targetStatus == fromStatus) {
            log.info("用户状态未变化，跳过发布事件，id={}，status={}", id, targetStatus);
            return Boolean.FALSE;
        }

        User update = new User();
        update.setId(id);
        update.setStatus(targetStatus.getCode());
        int rows = userMapper.updateById(update);

        if (rows > 0) {
            UserStatusChangeEvent event = new UserStatusChangeEvent(
                    id, user.getUsername(), fromStatus, targetStatus);
            userStatusChangePublisher.publish(event);
        }
        return rows > 0;
    }

    /**
     * 给用户分配角色（全量覆盖）
     * 全量覆盖策略：移除不在新列表中的旧关联，补齐新关联；同事务保证一致性
     *
     * @param assignDTO 用户分配角色参数
     * @return 是否分配成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"rbac:roles", "rbac:perms"}, key = "#assignDTO.userId")
    public Boolean assignRoles(UserRoleAssignDTO assignDTO) {
        Long userId = assignDTO.getUserId();
        List<Long> roleIds = assignDTO.getRoleIds();
        if (Objects.isNull(userId)) {
            throw new BizException(ResultCode.PARAM_VALIDATE_FAILED, "用户 ID 不能为空");
        }
        if (CollUtil.isEmpty(roleIds)) {
            throw new BizException(ResultCode.PARAM_VALIDATE_FAILED, "角色 ID 集合不能为空");
        }
        User user = userMapper.selectById(userId);
        if (Objects.isNull(user)) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        log.info("给用户分配角色，userId={}，roleIds={}", userId, roleIds);

        // 1. 查询当前用户已关联的角色 ID 集合
        LambdaQueryWrapper<UserRole> existWrapper = Wrappers.lambdaQuery(UserRole.class)
                .eq(UserRole::getUserId, userId);
        List<UserRole> existList = userRoleMapper.selectList(existWrapper);
        List<Long> existRoleIds = existList.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        // 2. 计算需要删除的差集（旧关联 - 新列表）
        List<Long> toRemoveIds = existRoleIds.stream()
                .filter(id -> !roleIds.contains(id))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(toRemoveIds)) {
            LambdaQueryWrapper<UserRole> removeWrapper = Wrappers.lambdaQuery(UserRole.class)
                    .eq(UserRole::getUserId, userId)
                    .in(UserRole::getRoleId, toRemoveIds);
            userRoleMapper.delete(removeWrapper);
            log.info("移除用户旧角色关联，userId={}，removedRoleIds={}", userId, toRemoveIds);
        }

        // 3. 计算需要新增的差集（新列表 - 旧关联）
        List<Long> toAddIds = roleIds.stream()
                .filter(id -> !existRoleIds.contains(id))
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(toAddIds)) {
            String createBy = "system";
            for (Long roleId : toAddIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateBy(createBy);
                userRoleMapper.insert(userRole);
            }
            log.info("新增用户角色关联，userId={}，addedRoleIds={}", userId, toAddIds);
        }
        return Boolean.TRUE;
    }

    /**
     * 查询用户已分配的角色列表
     *
     * @param userId 用户 ID
     * @return 用户角色视图对象（包含用户信息和角色集合）
     */
    @Override
    public UserRoleVO listUserRoles(Long userId) {
        log.info("查询用户角色列表，userId={}", userId);
        User user = userMapper.selectById(userId);
        if (Objects.isNull(user)) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        UserRoleVO vo = new UserRoleVO();
        vo.setUserId(userId);
        vo.setUsername(user.getUsername());
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        List<RoleVO> roleVOList = BeanUtil.copyToList(roles, RoleVO.class);
        vo.setRoles(CollUtil.isNotEmpty(roleVOList) ? roleVOList : CollUtil.newArrayList());
        return vo;
    }

    /**
     * 校验用户名是否唯一
     *
     * @param username 用户名
     * @param excludeId 排除的 ID（修改时传当前 ID，新增时传 null）
     */
    private void checkUsernameUnique(String username, Long excludeId) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username)
                .ne(Objects.nonNull(excludeId), User::getId, excludeId);
        Long count = userMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BIZ_ERROR, "用户名已存在");
        }
    }

    /**
     * 对 VO 中的敏感字段进行脱敏处理
     *
     * @param vo 用户视图对象
     */
    private void maskSensitiveField(UserVO vo) {
        if (StrUtil.isNotBlank(vo.getPhone())) {
            vo.setPhone(vo.getPhone().replaceAll(PHONE_MASK_REGEX, PHONE_MASK_REPLACEMENT));
        }
        if (StrUtil.isNotBlank(vo.getEmail())) {
            vo.setEmail(vo.getEmail().replaceAll(EMAIL_MASK_REGEX, EMAIL_MASK_REPLACEMENT));
        }
    }

}
