package org.dam.component.status.impl;

import lombok.extern.slf4j.Slf4j;
import org.dam.component.status.UserStatus;
import org.dam.component.status.UserStatusChangeEvent;
import org.dam.component.status.UserStatusChangeObserver;
import org.springframework.stereotype.Component;

/**
 * 用户禁用状态观察者
 * 处理用户被禁用时需要执行的逻辑：
 * 1. 强制下线（销毁 Token、踢出会话）
 * 2. 记录禁用审计日志
 * 3. 通知该用户的关联人
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Component
public class UserStatusDisabledHandler implements UserStatusChangeObserver {

    @Override
    public UserStatus targetStatus() {
        return UserStatus.DISABLED;
    }

    @Override
    public void onStatusChange(UserStatusChangeEvent event) {
        log.info("[禁用观察者] 处理用户禁用事件，userId={}，username={}，原状态={}",
                event.getUserId(), event.getUsername(), event.getFromStatus());

        // 1. 强制下线（伪实现，后续接入 TokenService / SessionManager）
        log.info("[禁用观察者] 强制下线，销毁 Token，userId={}，username={}",
                event.getUserId(), event.getUsername());

        // 2. 记录禁用审计日志（伪实现，后续接入 AuditLogService）
        log.info("[禁用观察者] 记录禁用审计日志，userId={}，changeTime={}",
                event.getUserId(), event.getChangeTime());

        // 3. 通知关联人（伪实现，后续接入 NotifyService）
        log.info("[禁用观察者] 已通知关联人，userId={}", event.getUserId());
    }

}
