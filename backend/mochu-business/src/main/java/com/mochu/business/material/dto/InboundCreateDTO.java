package com.mochu.business.material.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class InboundCreateDTO {
    private Long contractId;
    private Long projectId;
    private Long supplierId;
    private String warehouse;
    private String receiver;
    private List<InboundItemDTO> items;

    @Data
    public static class InboundItemDTO {
        private String materialName;
        private String spec;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private Long contractItemId;
        private String remark;
    }
}
