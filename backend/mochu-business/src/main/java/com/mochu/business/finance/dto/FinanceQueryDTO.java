package com.mochu.business.finance.dto;

import lombok.Data;

@Data
public class FinanceQueryDTO {
    private Long projectId;
    private Long contractId;
    private Integer status;
    private String period;
    private Integer page = 1;
    private Integer size = 20;
}
