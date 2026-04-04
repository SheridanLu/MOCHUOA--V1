package com.mochu.business.project.dto;

import lombok.Data;

@Data
public class ProjectQueryDTO {
    private String keyword;
    private Integer projectType;
    private Integer status;
    private Long deptId;
    private Integer page = 1;
    private Integer size = 20;
}
