package com.mochu.framework.aop;

import com.mochu.common.utils.SecurityUtils;
import com.mochu.common.audit.SysAuditLog;
import com.mochu.common.audit.SysAuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.*;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final SysAuditLogMapper auditLogMapper;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        Long userId = null;
        String username = null;
        String ip = null;
        try {
            userId = SecurityUtils.getUserId();
            username = SecurityUtils.getCurrentUsername();
        } catch (Exception ignored) {}
        try {
            ip = getClientIp();
        } catch (Exception ignored) {}

        try {
            Object result = point.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("AuditLog: user={}, module={}, action={}, elapsed={}ms",
                    username, auditLog.module(), auditLog.action(), elapsed);
            saveAuditLog(userId, username, auditLog.module(), auditLog.action(),
                    auditLog.targetType(), null, "success, elapsed=" + elapsed + "ms", ip);
            return result;
        } catch (Throwable e) {
            log.warn("AuditLog: user={}, module={}, action={}, error={}",
                    username, auditLog.module(), auditLog.action(), e.getMessage());
            saveAuditLog(userId, username, auditLog.module(), auditLog.action(),
                    auditLog.targetType(), null, "error: " + e.getMessage(), ip);
            throw e;
        }
    }

    private void saveAuditLog(Long userId, String username, String module, String action,
                              String targetType, Long targetId, String detail, String ip) {
        try {
            SysAuditLog log = new SysAuditLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setModule(module);
            log.setAction(action);
            log.setTargetType(targetType.isEmpty() ? null : targetType);
            log.setTargetId(targetId);
            log.setDetail(detail != null && detail.length() > 2000 ? detail.substring(0, 2000) : detail);
            log.setIp(ip);
            log.setCreatedAt(LocalDateTime.now());
            auditLogMapper.insert(log);
        } catch (Exception e) {
            AuditLogAspect.log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface AuditLog {
        String module();
        String action();
        String targetType() default "";
    }
}
