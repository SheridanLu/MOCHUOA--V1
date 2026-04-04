package com.mochu.business.project.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProjectUpdateDTO {
    private String projectName;
    private String description;
    private Long deptId;
    private BigDecimal bidAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal amountWithoutTax;
    private BigDecimal investLimit;
    private String bidNoticeUrl;
    private List<ProjectCreateDTO.PaymentBatchDTO> paymentBatches;
}
