package com.mochu.business.completion.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SettlementVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private String settlementNo;
    private BigDecimal contractAmount;
    private BigDecimal changeAmount;
    private BigDecimal finalAmount;
    private Integer status;
    private String statusName;
    private String creatorName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
