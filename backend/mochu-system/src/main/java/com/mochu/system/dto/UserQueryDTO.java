package com.mochu.system.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private String keyword;
    private Long deptId;
    private Long roleId;
    private Integer status;
    private Integer page = 1;
    private Integer size = 20;
}
