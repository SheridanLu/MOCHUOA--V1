package com.mochu.system.service;

import com.mochu.common.result.PageResult;
import com.mochu.system.dto.AuditLogQueryDTO;
import com.mochu.system.vo.AuditLogVO;
import jakarta.servlet.http.HttpServletResponse;

public interface AuditLogService {
    PageResult<AuditLogVO> listAuditLogs(AuditLogQueryDTO query);
    void exportExcel(AuditLogQueryDTO query, HttpServletResponse response);
    void exportJson(AuditLogQueryDTO query, HttpServletResponse response);
}
