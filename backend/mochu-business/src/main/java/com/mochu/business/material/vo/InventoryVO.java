package com.mochu.business.material.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InventoryVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private String materialName;
    private String spec;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal weightedAvgPrice;
    private BigDecimal totalAmount;
    private String warehouse;
}
