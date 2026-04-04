package com.mochu.business.progress.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TaskVO {
    private Long id;
    private Long planId;
    private Long projectId;
    private String taskName;
    private Long parentTaskId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate actualStart;
    private LocalDate actualEnd;
    private BigDecimal progress;
    private Integer status;
    private String statusName;
    private Long assigneeId;
    private String assigneeName;
    private Integer milestone;
    private Integer sortOrder;
    private List<TaskVO> children;
}
