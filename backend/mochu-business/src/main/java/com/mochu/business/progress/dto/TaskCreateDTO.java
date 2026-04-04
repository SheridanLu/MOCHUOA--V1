package com.mochu.business.progress.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskCreateDTO {
    @NotNull(message = "计划ID不能为空")
    private Long planId;
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    private Long parentTaskId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long assigneeId;
    private Integer milestone;
    private Integer sortOrder;
}
