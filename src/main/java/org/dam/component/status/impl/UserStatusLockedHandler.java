package org.dam.component.status.impl;

import lombok.extern.slf4j.Slf4j;
import org.dam.component.status.UserStatus;
import org.dam.component.status.UserStatusChangeEvent;
import org.dam.component.status.UserStatusChangeObserver;
import org.springframework.stereotype.Component;

/**
 * 用户锁定状态观察者
 * 处理用户被锁定时需要执行的逻辑：
 * 1. 风险告警通知安全管理员
 * 2. 记录锁定原因与触发场景
 * 3. 暂停该用户所有定时任务
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Component
public class UserStatusLockedHandler implements UserStatusChangeObserver {

    @Override
    public UserStatus targetStatus() {
        return UserStatus.LOCKED;
    }

    @Override
    public void onStatusChange(UserStatusChangeEvent event) {
        log.info("[锁定观察者] 处理用户锁定事件，userId={}，username={}，原状态={}",
                event.getUserId(), event.getUsername(), event.getFromStatus());

        // 1. 风险告警通知安全管理员（伪实现，后续接入 RiskAlertService）
        log.warn("[锁定观察者] 已触发风险告警，userId={}，changeTime={}",
                event.getUserId(), event.getChangeTime());

        // 2. 记录锁定原因与触发场景（伪实现，后续接入 LockReasonService）
        log.info("[锁定观察者] 记录锁定原因，userId={}，fromStatus={}",
                event.getUserId(), event.getFromStatus());

        // 3. 暂停该用户所有定时任务（伪实现，后续接入 JobScheduler）
        log.info("[锁定观察者] 已暂停用户关联的定时任务，userId={}", event.getUserId());
    }

}
