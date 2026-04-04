package com.mochu.business.progress.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviationVO {
    private Long id;
    private Long taskId;
    private String taskName;
    private Long projectId;
    private Integer deviationDays;
    private Integer deviationType;
    private String deviationTypeName;
    private String description;
    private LocalDateTime createdAt;
}
