package com.mochu.business.purchase.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseListVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long contractId;
    private String listNo;
    private Integer status;
    private String statusName;
    private BigDecimal totalAmount;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private List<PurchaseItemVO> items;

    @Data
    public static class PurchaseItemVO {
        private Long id;
        private String materialName;
        private String spec;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal estimatedPrice;
        private String remark;
    }
}
