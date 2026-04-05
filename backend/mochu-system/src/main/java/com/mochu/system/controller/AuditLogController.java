package com.mochu.system.controller;

import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import com.mochu.system.dto.AuditLogQueryDTO;
import com.mochu.system.service.AuditLogService;
import com.mochu.system.vo.AuditLogVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('audit:view')")
    public R<PageResult<AuditLogVO>> listAuditLogs(AuditLogQueryDTO query) {
        return R.ok(auditLogService.listAuditLogs(query));
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('audit:view')")
    public void exportExcel(AuditLogQueryDTO query, HttpServletResponse response) {
        auditLogService.exportExcel(query, response);
    }

    @GetMapping("/export/json")
    @PreAuthorize("hasAuthority('audit:view')")
    public void exportJson(AuditLogQueryDTO query, HttpServletResponse response) {
        auditLogService.exportJson(query, response);
    }
}
