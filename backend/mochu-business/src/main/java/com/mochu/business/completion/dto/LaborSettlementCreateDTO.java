package com.mochu.business.completion.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LaborSettlementCreateDTO {
    private Long projectId;
    private String teamName;
    private String laborType;
    private Integer totalDays;
    private BigDecimal dailyRate;
    private BigDecimal deduction;
}
