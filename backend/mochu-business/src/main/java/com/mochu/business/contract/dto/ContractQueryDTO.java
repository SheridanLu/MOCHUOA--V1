package com.mochu.business.contract.dto;

import lombok.Data;

@Data
public class ContractQueryDTO {
    private String keyword;
    private Integer contractType;
    private Integer status;
    private Long projectId;
    private Long supplierId;
    private Integer page = 1;
    private Integer size = 20;
}
