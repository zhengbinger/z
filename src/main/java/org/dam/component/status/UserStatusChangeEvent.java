package org.dam.component.status;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户状态变更事件
 * 由被观察者（事件发布器）在用户状态发生变更时创建并广播
 * 观察者根据该事件携带的信息执行对应处理逻辑
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Data
public class UserStatusChangeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private final Long userId;

    /**
     * 用户名
     */
    private final String username;

    /**
     * 变更前状态
     */
    private final UserStatus fromStatus;

    /**
     * 变更后状态
     */
    private final UserStatus toStatus;

    /**
     * 变更时间
     */
    private final LocalDateTime changeTime;

    public UserStatusChangeEvent(Long userId, String username, UserStatus fromStatus, UserStatus toStatus) {
        this.userId = userId;
        this.username = username;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changeTime = LocalDateTime.now();
    }

}
