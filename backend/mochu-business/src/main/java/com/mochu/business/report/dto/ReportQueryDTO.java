package com.mochu.business.report.dto;

import lombok.Data;

@Data
public class ReportQueryDTO {
    private Long projectId;
    private Long deptId;
    private String startDate;
    private String endDate;
    private String period;
    private String reportType;
}
