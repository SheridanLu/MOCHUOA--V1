package com.mochu.business.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentCreateDTO {
    @NotNull(message = "付款类型不能为空")
    private Integer paymentType;
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    private Long contractId;
    private Long supplierId;
    private Long reconciliationId;
    @NotNull(message = "付款金额不能为空")
    private BigDecimal amount;
    private String bankName;
    private String bankAccount;
    private String remark;
}
