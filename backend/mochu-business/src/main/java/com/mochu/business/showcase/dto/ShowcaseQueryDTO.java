package com.mochu.business.showcase.dto;

import lombok.Data;

@Data
public class ShowcaseQueryDTO {
    private Integer status;
    private Integer visibility;
    private Integer page = 1;
    private Integer size = 20;
}
