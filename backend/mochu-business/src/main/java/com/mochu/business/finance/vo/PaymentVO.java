package com.mochu.business.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentVO {
    private Long id;
    private String paymentNo;
    private Integer paymentType;
    private String paymentTypeName;
    private Long projectId;
    private String projectName;
    private Long contractId;
    private Long supplierId;
    private String supplierName;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private Integer status;
    private String statusName;
    private String bankName;
    private String bankAccount;
    private String remark;
    private LocalDateTime approvedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
