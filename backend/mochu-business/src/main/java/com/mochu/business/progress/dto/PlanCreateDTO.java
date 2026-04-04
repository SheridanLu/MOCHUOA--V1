package com.mochu.business.progress.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PlanCreateDTO {
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    @NotBlank(message = "计划名称不能为空")
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
}
