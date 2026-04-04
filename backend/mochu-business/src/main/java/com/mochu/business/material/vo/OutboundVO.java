package com.mochu.business.material.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OutboundVO {
    private Long id;
    private String outboundNo;
    private Long projectId;
    private String projectName;
    private String warehouse;
    private String recipient;
    private String purpose;
    private LocalDate outboundDate;
    private Integer status;
    private String statusName;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<OutboundItemVO> items;

    @Data
    public static class OutboundItemVO {
        private Long id;
        private String materialName;
        private String spec;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private String remark;
    }
}
