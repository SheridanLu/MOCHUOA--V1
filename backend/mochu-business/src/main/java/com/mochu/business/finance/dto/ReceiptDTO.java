package com.mochu.business.finance.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReceiptDTO {
    private Long projectId;
    private Long contractId;
    private BigDecimal amount;
    private String remark;
}
