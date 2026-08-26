package org.dam.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.dam.component.security.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 字段自动填充处理器
 * 在 INSERT/UPDATE 时自动填充 createTime、updateTime、createBy、updateBy 字段
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 新增时自动填充
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUser());
        this.strictInsertFill(metaObject, "updateBy", String.class, getCurrentUser());
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    /**
     * 更新时自动填充
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUser());
    }

    /**
     * 获取当前操作用户
     * 优先从 {@link SecurityContextHolder} 中读取登录用户名；
     * 若未登录（如系统初始化任务、单元测试、非 HTTP 请求线程等场景），则兜底返回 "system"
     *
     * @return 当前用户标识（用户名）
     */
    private String getCurrentUser() {
        if (SecurityContextHolder.isLoggedIn()) {
            String username = SecurityContextHolder.getCurrentUsername();
            return username != null ? username : "system";
        }
        return "system";
    }

}
