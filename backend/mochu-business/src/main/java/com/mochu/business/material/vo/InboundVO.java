package com.mochu.business.material.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InboundVO {
    private Long id;
    private String inboundNo;
    private Long contractId;
    private Long projectId;
    private String projectName;
    private Long supplierId;
    private String supplierName;
    private String warehouse;
    private String receiver;
    private LocalDate inboundDate;
    private Integer status;
    private String statusName;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<InboundItemVO> items;

    @Data
    public static class InboundItemVO {
        private Long id;
        private String materialName;
        private String spec;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private Long contractItemId;
        private String remark;
    }
}
