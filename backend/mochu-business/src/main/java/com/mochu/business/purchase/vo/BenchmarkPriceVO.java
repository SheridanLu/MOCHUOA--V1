package com.mochu.business.purchase.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BenchmarkPriceVO {
    private Long id;
    private String materialName;
    private String spec;
    private String unit;
    private BigDecimal benchmarkPrice;
    private Integer updateType;
    private String updateTypeName;
    private Long sourceContractId;
    private Long sourceSupplierId;
    private String supplierName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
