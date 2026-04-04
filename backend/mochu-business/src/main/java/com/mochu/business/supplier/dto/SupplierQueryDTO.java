package com.mochu.business.supplier.dto;

import lombok.Data;

@Data
public class SupplierQueryDTO {
    private String keyword;
    private Integer category;
    private Integer status;
    private Integer page = 1;
    private Integer size = 20;
}
