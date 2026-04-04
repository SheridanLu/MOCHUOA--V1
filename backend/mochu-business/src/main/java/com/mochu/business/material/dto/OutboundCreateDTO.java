package com.mochu.business.material.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OutboundCreateDTO {
    private Long projectId;
    private String warehouse;
    private String recipient;
    private String purpose;
    private List<OutboundItemDTO> items;

    @Data
    public static class OutboundItemDTO {
        private String materialName;
        private String spec;
        private String unit;
        private BigDecimal quantity;
        private String remark;
    }
}
