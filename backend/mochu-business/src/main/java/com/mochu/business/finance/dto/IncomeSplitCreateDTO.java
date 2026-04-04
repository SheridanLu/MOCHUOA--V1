package com.mochu.business.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class IncomeSplitCreateDTO {
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    @NotNull(message = "合同ID不能为空")
    private Long contractId;
    private String period;
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;
}
