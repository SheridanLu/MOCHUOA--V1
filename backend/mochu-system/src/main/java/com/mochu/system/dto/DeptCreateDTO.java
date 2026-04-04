package com.mochu.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeptCreateDTO {
    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    @NotNull(message = "上级部门不能为空")
    private Long parentId;
    private Integer sortOrder = 0;
    private Long leaderId;
}
