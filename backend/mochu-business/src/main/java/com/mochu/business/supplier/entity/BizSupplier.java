package com.mochu.business.supplier.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_supplier")
public class BizSupplier extends BaseEntity {
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
    private Long creatorId;
}
