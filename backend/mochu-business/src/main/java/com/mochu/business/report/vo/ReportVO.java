package com.mochu.business.report.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportVO {
    private String reportType;
    private String reportName;
    private String period;
    private LocalDateTime generatedAt;
    private Object data;
}
