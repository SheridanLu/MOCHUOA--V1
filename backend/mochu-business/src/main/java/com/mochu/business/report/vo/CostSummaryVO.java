package com.mochu.business.report.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CostSummaryVO {
    private Long projectId;
    private String projectName;
    private BigDecimal materialCost;
    private BigDecimal laborCost;
    private BigDecimal equipmentCost;
    private BigDecimal managementCost;
    private BigDecimal otherCost;
    private BigDecimal totalCost;
    private String period;
}
