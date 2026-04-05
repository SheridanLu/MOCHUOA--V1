package com.mochu.business.completion.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LaborSettlementVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private String settlementNo;
    private String teamName;
    private String laborType;
    private Integer totalDays;
    private BigDecimal dailyRate;
    private BigDecimal totalAmount;
    private BigDecimal deduction;
    private BigDecimal finalAmount;
    private Integer status;
    private String statusName;
    private String creatorName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
