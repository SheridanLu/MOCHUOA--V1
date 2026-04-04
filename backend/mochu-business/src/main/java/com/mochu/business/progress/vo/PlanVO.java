package com.mochu.business.progress.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PlanVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private String statusName;
    private LocalDateTime createdAt;
}
