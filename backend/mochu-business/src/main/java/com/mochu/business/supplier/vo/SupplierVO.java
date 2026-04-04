package com.mochu.business.supplier.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SupplierVO {
    private Long id;
    private String supplierName;
    private String supplierCode;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private String bankName;
    private String bankAccount;
    private String taxNo;
    private Integer category;
    private Integer rating;
    private Integer status;
    private LocalDateTime createdAt;
}
