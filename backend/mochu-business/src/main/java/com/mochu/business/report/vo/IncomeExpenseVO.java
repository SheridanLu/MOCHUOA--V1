package com.mochu.business.report.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IncomeExpenseVO {
    private Long projectId;
    private String projectName;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal profit;
    private BigDecimal profitRate;
    private String period;
}
