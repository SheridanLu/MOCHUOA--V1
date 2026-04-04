package com.mochu.business.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReconciliationVO {
    private Long id;
    private String reconciliationNo;
    private Long projectId;
    private String projectName;
    private Long supplierId;
    private String supplierName;
    private Long contractId;
    private String period;
    private BigDecimal totalAmount;
    private BigDecimal confirmedAmount;
    private BigDecimal difference;
    private Integer status;
    private String statusName;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
}
