package org.dam.component.status.impl;

import lombok.extern.slf4j.Slf4j;
import org.dam.component.status.UserStatus;
import org.dam.component.status.UserStatusChangeEvent;
import org.dam.component.status.UserStatusChangeObserver;
import org.springframework.stereotype.Component;

/**
 * 用户待审核状态观察者
 * 处理用户进入待审核状态时需要执行的逻辑：
 * 1. 向审核员发送待审邮件
 * 2. 生成审核任务
 * 3. 记录提交审核时间
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Component
public class UserStatusPendingHandler implements UserStatusChangeObserver {

    @Override
    public UserStatus targetStatus() {
        return UserStatus.PENDING;
    }

    @Override
    public void onStatusChange(UserStatusChangeEvent event) {
        log.info("[待审核观察者] 处理用户进入待审核事件，userId={}，username={}，原状态={}",
                event.getUserId(), event.getUsername(), event.getFromStatus());

        // 1. 向审核员发送待审邮件（伪实现，后续接入 AuditNotifyService）
        log.info("[待审核观察者] 已向审核员发送待审邮件，userId={}，submitTime={}",
                event.getUserId(), event.getChangeTime());

        // 2. 生成审核任务（伪实现，后续接入 AuditTaskService）
        log.info("[待审核观察者] 已生成审核任务，userId={}", event.getUserId());

        // 3. 记录提交审核时间（伪实现，后续接入 AuditLogService）
        log.info("[待审核观察者] 已记录提交审核时间，userId={}，changeTime={}",
                event.getUserId(), event.getChangeTime());
    }

}
