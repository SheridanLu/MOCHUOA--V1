package com.mochu.business.finance.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CostVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private Integer costType;
    private String costTypeName;
    private String category;
    private BigDecimal amount;
    private String description;
    private String period;
}
