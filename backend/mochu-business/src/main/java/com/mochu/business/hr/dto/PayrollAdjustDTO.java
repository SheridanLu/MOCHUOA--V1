package com.mochu.business.hr.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PayrollAdjustDTO {
    private BigDecimal bonus;
    private BigDecimal deduction;
    private String reason;
}
