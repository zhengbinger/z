package org.dam.component.security.aspect;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.dam.common.enums.ResultCode;
import org.dam.common.exception.BizException;
import org.dam.component.security.SecurityContextHolder;
import org.dam.component.security.annotation.Logical;
import org.dam.component.security.annotation.RequiresLogin;
import org.dam.component.security.annotation.RequiresPermission;
import org.dam.component.security.annotation.RequiresRole;
import org.dam.service.AccessControlService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * RBAC 权限校验切面
 * 拦截 {@link RequiresLogin} / {@link RequiresRole} / {@link RequiresPermission} 注解
 * 方法注解优先于类注解；类 + 方法同时存在时，方法注解生效，类注解被忽略
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Slf4j
@Aspect
@Component
public class RbacAspect {

    @Resource
    private AccessControlService accessControlService;

    /**
     * 切点：标注了 @RequiresLogin 的方法或类
     */
    @Pointcut("@annotation(org.dam.component.security.annotation.RequiresLogin) "
            + "|| @within(org.dam.component.security.annotation.RequiresLogin)")
    public void requiresLoginPointcut() {
    }

    /**
     * 切点：标注了 @RequiresRole 的方法或类
     */
    @Pointcut("@annotation(org.dam.component.security.annotation.RequiresRole) "
            + "|| @within(org.dam.component.security.annotation.RequiresRole)")
    public void requiresRolePointcut() {
    }

    /**
     * 切点：标注了 @RequiresPermission 的方法或类
     */
    @Pointcut("@annotation(org.dam.component.security.annotation.RequiresPermission) "
            + "|| @within(org.dam.component.security.annotation.RequiresPermission)")
    public void requiresPermissionPointcut() {
    }

    @Before("requiresLoginPointcut()")
    public void checkLogin(JoinPoint joinPoint) {
        if (!SecurityContextHolder.isLoggedIn()) {
            log.warn("未登录访问受保护资源，method={}", joinPoint.getSignature().toShortString());
            throw new BizException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
        }
    }

    @Before("requiresRolePointcut()")
    public void checkRole(JoinPoint joinPoint) {
        // 先校验登录
        checkLogin(joinPoint);

        RequiresRole anno = resolveAnnotation(joinPoint, RequiresRole.class);
        if (anno == null) {
            return;
        }
        Long userId = SecurityContextHolder.getCurrentUserId();
        String[] roles = anno.value();
        Logical logical = anno.logical();

        if (logical == Logical.AND) {
            for (String role : roles) {
                if (!accessControlService.hasRole(userId, role)) {
                    log.warn("权限校验失败，缺少角色 {}，userId={}", role, userId);
                    throw new BizException(ResultCode.FORBIDDEN, "无权限访问，缺少角色：" + role);
                }
            }
        } else {
            if (!accessControlService.hasAnyRole(userId, roles)) {
                log.warn("权限校验失败，缺少任意一个角色 {}，userId={}", String.join("/", roles), userId);
                throw new BizException(ResultCode.FORBIDDEN, "无权限访问，缺少所需角色");
            }
        }
    }

    @Before("requiresPermissionPointcut()")
    public void checkPermission(JoinPoint joinPoint) {
        // 先校验登录
        checkLogin(joinPoint);

        RequiresPermission anno = resolveAnnotation(joinPoint, RequiresPermission.class);
        if (anno == null) {
            return;
        }
        Long userId = SecurityContextHolder.getCurrentUserId();
        String[] perms = anno.value();
        Logical logical = anno.logical();

        if (logical == Logical.AND) {
            for (String perm : perms) {
                if (!accessControlService.hasPermission(userId, perm)) {
                    log.warn("权限校验失败，缺少权限 {}，userId={}", perm, userId);
                    throw new BizException(ResultCode.FORBIDDEN, "无权限访问，缺少权限：" + perm);
                }
            }
        } else {
            if (!accessControlService.hasAnyPermission(userId, perms)) {
                log.warn("权限校验失败，缺少任意一个权限 {}，userId={}", String.join("/", perms), userId);
                throw new BizException(ResultCode.FORBIDDEN, "无权限访问，缺少所需权限");
            }
        }
    }

    /**
     * 解析方法或类上的注解（方法优先）
     */
    private <A extends java.lang.annotation.Annotation> A resolveAnnotation(JoinPoint joinPoint, Class<A> annotationType) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        A methodAnno = method.getAnnotation(annotationType);
        if (methodAnno != null) {
            return methodAnno;
        }
        Class<?> targetClass = joinPoint.getTarget().getClass();
        return targetClass.getAnnotation(annotationType);
    }

}
