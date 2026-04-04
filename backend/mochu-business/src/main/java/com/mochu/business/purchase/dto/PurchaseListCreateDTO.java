package com.mochu.business.purchase.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseListCreateDTO {
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    private Long contractId;
    private List<PurchaseItemDTO> items;

    @Data
    public static class PurchaseItemDTO {
        private String materialName;
        private String spec;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal estimatedPrice;
        private String remark;
    }
}
