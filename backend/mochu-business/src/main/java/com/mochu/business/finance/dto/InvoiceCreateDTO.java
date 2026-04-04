package com.mochu.business.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceCreateDTO {
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    private Long contractId;
    private String invoiceNo;
    private Integer invoiceType;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private LocalDate invoiceDate;
    private String fileUrl;
}
