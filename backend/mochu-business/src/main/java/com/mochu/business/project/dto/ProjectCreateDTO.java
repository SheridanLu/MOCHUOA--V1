package com.mochu.business.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProjectCreateDTO {
    @NotNull(message = "项目类型不能为空")
    private Integer projectType;
    @NotBlank(message = "项目名称不能为空")
    private String projectName;
    private String description;
    private Long deptId;
    private BigDecimal bidAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal amountWithoutTax;
    private BigDecimal investLimit;
    private String bidNoticeUrl;
    private List<PaymentBatchDTO> paymentBatches;

    @Data
    public static class PaymentBatchDTO {
        private Integer batchNo;
        private String description;
        private BigDecimal ratio;
        private BigDecimal amount;
        private String plannedDate;
    }
}
