package com.mochu.business.purchase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BenchmarkPriceDTO {
    @NotBlank(message = "物资名称不能为空")
    private String materialName;
    private String spec;
    private String unit;
    @NotNull(message = "基准价不能为空")
    private BigDecimal benchmarkPrice;
}
