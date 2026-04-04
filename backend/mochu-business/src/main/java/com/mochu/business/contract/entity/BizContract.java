package com.mochu.business.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_contract")
public class BizContract extends BaseEntity {
    private String contractNo;
    private String contractName;
    private Integer contractType;
    private Long projectId;
    private Long parentContractId;
    private Long supplierId;
    private BigDecimal amountWithTax;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal amountWithoutTax;
    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private Long templateId;
    private String fileUrl;
    private String remark;
    private Long creatorId;
}
