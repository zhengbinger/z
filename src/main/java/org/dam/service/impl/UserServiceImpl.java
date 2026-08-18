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
import org.dam.dto.UserSaveDTO;
import org.dam.entity.User;
import org.dam.mapper.UserMapper;
import org.dam.service.UserService;
import org.dam.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

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
