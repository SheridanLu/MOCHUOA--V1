package com.mochu.business.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InvoiceVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long contractId;
    private String invoiceNo;
    private Integer invoiceType;
    private String invoiceTypeName;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private LocalDate invoiceDate;
    private String fileUrl;
    private Long daysToExpire;
    private LocalDateTime createdAt;
}
