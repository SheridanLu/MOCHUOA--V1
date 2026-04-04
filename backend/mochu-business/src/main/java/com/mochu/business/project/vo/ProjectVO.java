package com.mochu.business.project.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectVO {
    private Long id;
    private String projectNo;
    private String projectName;
    private Integer projectType;
    private String projectTypeName;
    private Integer status;
    private String statusName;
    private String description;
    private Long ownerId;
    private String ownerName;
    private Long deptId;
    private String deptName;
    private BigDecimal bidAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal amountWithoutTax;
    private BigDecimal investLimit;
    private String bidNoticeUrl;
    private String terminationReason;
    private Long costTargetProjectId;
    private LocalDateTime approvedAt;
    private LocalDateTime pausedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private List<PaymentBatchVO> paymentBatches;

    @Data
    public static class PaymentBatchVO {
        private Long id;
        private Integer batchNo;
        private String description;
        private BigDecimal ratio;
        private BigDecimal amount;
        private String plannedDate;
        private Integer status;
    }
}
