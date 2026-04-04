package com.mochu.framework.aop;

import com.mochu.common.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        String username = SecurityUtils.getCurrentUsername();
        try {
            Object result = point.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("AuditLog: user={}, module={}, action={}, elapsed={}ms",
                    username, auditLog.module(), auditLog.action(), elapsed);
            return result;
        } catch (Throwable e) {
            log.warn("AuditLog: user={}, module={}, action={}, error={}",
                    username, auditLog.module(), auditLog.action(), e.getMessage());
            throw e;
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface AuditLog {
        String module();
        String action();
    }
}
