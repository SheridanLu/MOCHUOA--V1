package com.mochu.business.completion.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CompletionReportVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private String reportNo;
    private LocalDate completionDate;
    private Integer qualityRating;
    private String summary;
    private String fileUrl;
    private Integer status;
    private String statusName;
    private String creatorName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
