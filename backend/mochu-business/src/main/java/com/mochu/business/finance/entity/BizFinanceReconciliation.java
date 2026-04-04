package com.mochu.business.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_finance_reconciliation")
public class BizFinanceReconciliation extends BaseEntity {
    private String reconciliationNo;
    private Long projectId;
    private Long supplierId;
    private Long contractId;
    private String period;
    private BigDecimal totalAmount;
    private BigDecimal confirmedAmount;
    private BigDecimal difference;
    private Integer status;
    private LocalDateTime confirmedAt;
    private Long creatorId;
}
