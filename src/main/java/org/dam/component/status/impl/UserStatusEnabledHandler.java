package org.dam.component.status.impl;

import lombok.extern.slf4j.Slf4j;
import org.dam.component.status.UserStatus;
import org.dam.component.status.UserStatusChangeEvent;
import org.dam.component.status.UserStatusChangeObserver;
import org.springframework.stereotype.Component;

/**
 * 用户启用状态观察者
 * 处理用户被启用时需要执行的逻辑：
 * 1. 清空登录失败次数
 * 2. 重置风控标记
 * 3. 发送启用通知
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Component
public class UserStatusEnabledHandler implements UserStatusChangeObserver {

    @Override
    public UserStatus targetStatus() {
        return UserStatus.ENABLED;
    }

    @Override
    public void onStatusChange(UserStatusChangeEvent event) {
        log.info("[启用观察者] 处理用户启用事件，userId={}，username={}，原状态={}",
                event.getUserId(), event.getUsername(), event.getFromStatus());

        // 1. 清空登录失败次数（伪实现，后续接入 LoginLogService）
        log.info("[启用观察者] 清空用户登录失败次数，userId={}", event.getUserId());

        // 2. 重置风控标记
        log.info("[启用观察者] 重置风控标记，userId={}", event.getUserId());

        // 3. 发送启用通知邮件（伪实现，后续接入 MailService）
        log.info("[启用观察者] 已发送启用通知邮件，userId={}，username={}",
                event.getUserId(), event.getUsername());
    }

}
