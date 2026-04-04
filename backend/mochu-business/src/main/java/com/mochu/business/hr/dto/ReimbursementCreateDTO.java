package com.mochu.business.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReimbursementCreateDTO {
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;
    private Long projectId;
    @NotBlank(message = "报销类别不能为空")
    private String category;
    @NotNull(message = "报销金额不能为空")
    private BigDecimal amount;
    private String description;
    private String fileUrl;
}
