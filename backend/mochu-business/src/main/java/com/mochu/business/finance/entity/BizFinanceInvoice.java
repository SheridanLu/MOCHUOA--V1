package com.mochu.business.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_finance_invoice")
public class BizFinanceInvoice extends BaseEntity {
    private Long projectId;
    private Long contractId;
    private String invoiceNo;
    private Integer invoiceType;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private LocalDate invoiceDate;
    private String fileUrl;
    private Long creatorId;
}
