package com.mochu.business.purchase.dto;

import lombok.Data;

@Data
public class PurchaseListQueryDTO {
    private String keyword;
    private Long projectId;
    private Integer status;
    private Integer page = 1;
    private Integer size = 20;
}
