package org.dam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dam.component.status.UserStatus;
import org.dam.dto.UserPageDTO;
import org.dam.dto.UserSaveDTO;
import org.dam.entity.User;
import org.dam.vo.UserVO;

/**
 * 用户服务接口
 * 定义用户相关的业务操作
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
public interface UserService {

    /**
     * 分页查询用户
     *
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    Page<UserVO> pageUser(UserPageDTO pageDTO);

    /**
     * 根据 ID 查询用户详情
     *
     * @param id 用户 ID
     * @return 用户视图对象
     */
    UserVO getUserById(Long id);

    /**
     * 新增用户
     *
     * @param saveDTO 用户保存参数
     * @return 新增后的用户 ID
     */
    Long saveUser(UserSaveDTO saveDTO);

    /**
     * 修改用户
     *
     * @param saveDTO 用户保存参数（必须包含 ID）
     * @return 是否修改成功
     */
    Boolean updateUser(UserSaveDTO saveDTO);

    /**
     * 根据 ID 逻辑删除用户
     *
     * @param id 用户 ID
     * @return 是否删除成功
     */
    Boolean removeUser(Long id);

    /**
     * 变更用户状态
     * 状态实际发生变化时，将通过观察者模式发布状态变更事件，
     * 触发对应状态的处理器执行后续业务逻辑（如强制下线、清空失败次数等）
     *
     * @param id           用户 ID
     * @param targetStatus 目标状态枚举
     * @return 是否变更成功（状态未变化时返回 false）
     */
    Boolean changeUserStatus(Long id, UserStatus targetStatus);

}
