package com.mochu.business.finance.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IncomeSplitVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long contractId;
    private String contractName;
    private String splitNo;
    private String period;
    private BigDecimal amount;
    private Integer status;
    private String statusName;
    private LocalDateTime createdAt;
}
