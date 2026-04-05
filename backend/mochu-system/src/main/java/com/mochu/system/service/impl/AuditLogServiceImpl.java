package com.mochu.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.common.result.PageResult;
import com.mochu.system.dto.AuditLogQueryDTO;
import com.mochu.system.entity.SysAuditLog;
import com.mochu.system.mapper.SysAuditLogMapper;
import com.mochu.system.service.AuditLogService;
import com.mochu.system.vo.AuditLogVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final SysAuditLogMapper auditLogMapper;

    private static final String[] ACTION_TYPES = {
        "LOGIN", "LOGOUT", "CREATE", "UPDATE", "DELETE", "APPROVE", "REJECT", "EXPORT"
    };

    @Override
    @Transactional(readOnly = true)
    public PageResult<AuditLogVO> listAuditLogs(AuditLogQueryDTO query) {
        Page<SysAuditLog> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<SysAuditLog> wrapper = buildWrapper(query);
        wrapper.orderByDesc(SysAuditLog::getCreatedAt);
        Page<SysAuditLog> result = auditLogMapper.selectPage(page, wrapper);
        List<AuditLogVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public void exportExcel(AuditLogQueryDTO query, HttpServletResponse response) {
        query.setPage(1);
        query.setSize(100000); // max 10万行
        LambdaQueryWrapper<SysAuditLog> wrapper = buildWrapper(query);
        wrapper.orderByDesc(SysAuditLog::getCreatedAt);
        List<SysAuditLog> logs = auditLogMapper.selectList(wrapper);

        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=audit_log_" +
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID,用户,模块,操作,目标类型,目标ID,详情,IP,时间");
            for (SysAuditLog l : logs) {
                writer.printf("%d,%s,%s,%s,%s,%s,\"%s\",%s,%s%n",
                    l.getId(),
                    escape(l.getUsername()),
                    escape(l.getModule()),
                    escape(l.getAction()),
                    escape(l.getTargetType()),
                    l.getTargetId() != null ? l.getTargetId() : "",
                    escape(l.getDetail()),
                    escape(l.getIp()),
                    l.getCreatedAt() != null ? l.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : ""
                );
            }
            writer.flush();
            log.info("导出审计日志 {} 条", logs.size());
        } catch (IOException e) {
            log.error("导出审计日志失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void exportJson(AuditLogQueryDTO query, HttpServletResponse response) {
        query.setPage(1);
        query.setSize(100000);
        LambdaQueryWrapper<SysAuditLog> wrapper = buildWrapper(query);
        wrapper.orderByDesc(SysAuditLog::getCreatedAt);
        List<SysAuditLog> logs = auditLogMapper.selectList(wrapper);
        List<AuditLogVO> vos = logs.stream().map(this::toVO).collect(Collectors.toList());

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=audit_log_" +
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".json");

        try (PrintWriter writer = response.getWriter()) {
            writer.print("[");
            for (int i = 0; i < vos.size(); i++) {
                AuditLogVO v = vos.get(i);
                if (i > 0) writer.print(",");
                writer.printf("{\"id\":%d,\"username\":\"%s\",\"module\":\"%s\",\"action\":\"%s\",\"targetType\":\"%s\",\"targetId\":%s,\"detail\":\"%s\",\"ip\":\"%s\",\"createdAt\":\"%s\"}",
                    v.getId(),
                    jsonEscape(v.getUsername()),
                    jsonEscape(v.getModule()),
                    jsonEscape(v.getAction()),
                    jsonEscape(v.getTargetType()),
                    v.getTargetId() != null ? v.getTargetId() : "null",
                    jsonEscape(v.getDetail()),
                    jsonEscape(v.getIp()),
                    v.getCreatedAt() != null ? v.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : ""
                );
            }
            writer.print("]");
            writer.flush();
            log.info("导出审计日志JSON {} 条", vos.size());
        } catch (IOException e) {
            log.error("导出审计日志JSON失败: {}", e.getMessage());
        }
    }

    private LambdaQueryWrapper<SysAuditLog> buildWrapper(AuditLogQueryDTO query) {
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<>();
        if (query.getUserId() != null) wrapper.eq(SysAuditLog::getUserId, query.getUserId());
        if (query.getUsername() != null && !query.getUsername().isEmpty()) {
            wrapper.like(SysAuditLog::getUsername, query.getUsername());
        }
        if (query.getModule() != null && !query.getModule().isEmpty()) {
            wrapper.eq(SysAuditLog::getModule, query.getModule());
        }
        if (query.getAction() != null && !query.getAction().isEmpty()) {
            wrapper.eq(SysAuditLog::getAction, query.getAction());
        }
        if (query.getTargetType() != null && !query.getTargetType().isEmpty()) {
            wrapper.eq(SysAuditLog::getTargetType, query.getTargetType());
        }
        if (query.getStartDate() != null && !query.getStartDate().isEmpty()) {
            LocalDateTime start = LocalDate.parse(query.getStartDate()).atStartOfDay();
            wrapper.ge(SysAuditLog::getCreatedAt, start);
        }
        if (query.getEndDate() != null && !query.getEndDate().isEmpty()) {
            LocalDateTime end = LocalDate.parse(query.getEndDate()).atTime(LocalTime.MAX);
            wrapper.le(SysAuditLog::getCreatedAt, end);
        }
        return wrapper;
    }

    private AuditLogVO toVO(SysAuditLog l) {
        AuditLogVO vo = new AuditLogVO();
        vo.setId(l.getId());
        vo.setUserId(l.getUserId());
        vo.setUsername(l.getUsername());
        vo.setModule(l.getModule());
        vo.setAction(l.getAction());
        vo.setTargetType(l.getTargetType());
        vo.setTargetId(l.getTargetId());
        vo.setDetail(l.getDetail());
        vo.setIp(l.getIp());
        vo.setCreatedAt(l.getCreatedAt());
        return vo;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
    }

    private String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
