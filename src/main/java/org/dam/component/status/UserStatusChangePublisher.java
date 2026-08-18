package org.dam.component.status;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 用户状态变更事件发布器（被观察者/主题）
 * 持有所有 {@link UserStatusChangeObserver} 实现，
 * 当用户状态发生变更时，根据事件的 toStatus 路由分发到对应观察者执行处理逻辑
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Component
public class UserStatusChangePublisher {

    private final List<UserStatusChangeObserver> observers;

    @Autowired
    public UserStatusChangePublisher(List<UserStatusChangeObserver> observers) {
        this.observers = observers;
    }

    /**
     * 启动时打印已注册的观察者清单，便于确认观察者是否被 Spring 正确装配
     */
    @PostConstruct
    public void init() {
        log.info("UserStatusChangePublisher 已注册观察者数量={}，清单如下：", observers.size());
        for (UserStatusChangeObserver observer : observers) {
            log.info("  观察者={}，关注状态={}", observer.getClass().getSimpleName(), observer.targetStatus());
        }
    }

    /**
     * 发布用户状态变更事件
     * 按 toStatus 路由分发到关注该状态的观察者
     * 单个观察者抛异常不影响其他观察者执行
     *
     * @param event 状态变更事件
     */
    public void publish(UserStatusChangeEvent event) {
        if (event == null || event.getToStatus() == null) {
            log.warn("状态变更事件为空或目标状态为空，跳过发布");
            return;
        }
        log.info("发布用户状态变更事件，userId={}，username={}，{}->{}",
                event.getUserId(), event.getUsername(),
                event.getFromStatus(), event.getToStatus());

        for (UserStatusChangeObserver observer : observers) {
            if (observer.targetStatus() != event.getToStatus()) {
                continue;
            }
            try {
                observer.onStatusChange(event);
            } catch (Exception e) {
                log.error("观察者执行异常，观察者={}，userId={}",
                        observer.getClass().getSimpleName(), event.getUserId(), e);
            }
        }
    }

}
