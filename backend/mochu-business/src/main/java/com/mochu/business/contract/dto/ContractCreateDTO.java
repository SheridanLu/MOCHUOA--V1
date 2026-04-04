package com.mochu.business.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ContractCreateDTO {
    @NotBlank(message = "合同名称不能为空")
    private String contractName;
    @NotNull(message = "合同类型不能为空")
    private Integer contractType;
    @NotNull(message = "所属项目不能为空")
    private Long projectId;
    private Long supplierId;
    private BigDecimal amountWithTax;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal amountWithoutTax;
    private String signDate;
    private String startDate;
    private String endDate;
    private Long templateId;
    private String remark;
    private List<ContractItemDTO> items;

    @Data
    public static class ContractItemDTO {
        private String materialName;
        private String spec;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private String remark;
    }
}
