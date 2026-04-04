package com.mochu.business.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierCreateDTO {
    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;
    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private String bankName;
    private String bankAccount;
    private String taxNo;
    private Integer category;
    private Integer rating;
}
