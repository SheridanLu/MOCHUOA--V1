package com.mochu.business.hr.dto;

import lombok.Data;

@Data
public class EmployeeQueryDTO {
    private String keyword;
    private Long deptId;
    private Integer status;
    private Integer page = 1;
    private Integer size = 20;
}
