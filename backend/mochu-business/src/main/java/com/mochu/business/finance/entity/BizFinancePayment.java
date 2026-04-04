package com.mochu.business.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_finance_payment")
public class BizFinancePayment extends BaseEntity {
    private String paymentNo;
    private Integer paymentType;
    private Long projectId;
    private Long contractId;
    private Long supplierId;
    private Long reconciliationId;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private Integer status;
    private String bankName;
    private String bankAccount;
    private String remark;
    private Long creatorId;
    private Long approverId;
    private LocalDateTime approvedAt;
    private LocalDateTime paidAt;
}
