package com.mochu.business.material.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ReturnCreateDTO {
    private Long projectId;
    /** 1=on-site disposal, 2=return to manufacturer, 3=company warehouse, 4=inter-project transfer */
    private Integer returnType;
    private Long targetProjectId;
    private String reason;
    private List<ReturnItemDTO> items;

    @Data
    public static class ReturnItemDTO {
        private Long inboundItemId;
        private String materialName;
        private String spec;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
    }
}
