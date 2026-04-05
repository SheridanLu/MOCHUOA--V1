package com.mochu.business.completion.dto;

import lombok.Data;

@Data
public class CompletionQueryDTO {
    private Long projectId;
    private Integer status;
    private Integer page = 1;
    private Integer size = 20;
}
