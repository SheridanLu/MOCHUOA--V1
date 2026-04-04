package com.mochu.business.contract.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SupplementCreateDTO {
    @NotBlank(message = "补充原因不能为空")
    private String reason;
    private BigDecimal amountChange;
    private BigDecimal newTotal;
    private String fileUrl;
}
