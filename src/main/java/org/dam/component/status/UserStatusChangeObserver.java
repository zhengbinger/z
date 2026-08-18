package org.dam.component.status;

/**
 * 用户状态变更观察者接口
 * 实现类声明关注的 {@link #targetStatus()}，发布器据此路由分发事件
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
public interface UserStatusChangeObserver {

    /**
     * 该观察者关注的目标状态
     * 仅当事件中的 toStatus 与此值一致时，该观察者才会被触发
     *
     * @return 关注的目标状态枚举
     */
    UserStatus targetStatus();

    /**
     * 状态变更事件处理逻辑
     *
     * @param event 状态变更事件
     */
    void onStatusChange(UserStatusChangeEvent event);

}
