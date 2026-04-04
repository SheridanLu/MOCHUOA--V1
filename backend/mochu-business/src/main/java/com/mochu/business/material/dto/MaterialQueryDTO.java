package com.mochu.business.material.dto;

import lombok.Data;

@Data
public class MaterialQueryDTO {
    private String keyword;
    private Long projectId;
    private Integer status;
    private Integer page = 1;
    private Integer size = 20;
}
