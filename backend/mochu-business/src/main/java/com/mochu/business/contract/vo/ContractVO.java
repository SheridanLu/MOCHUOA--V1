package com.mochu.business.contract.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContractVO {
    private Long id;
    private String contractNo;
    private String contractName;
    private Integer contractType;
    private String contractTypeName;
    private Long projectId;
    private String projectName;
    private Long supplierId;
    private String supplierName;
    private BigDecimal amountWithTax;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal amountWithoutTax;
    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private String statusName;
    private String remark;
    private LocalDateTime createdAt;
    private List<ContractItemVO> items;
    private List<SupplementVO> supplements;

    @Data
    public static class ContractItemVO {
        private Long id;
        private String materialName;
        private String spec;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private String remark;
    }

    @Data
    public static class SupplementVO {
        private Long id;
        private String supplementNo;
        private String reason;
        private BigDecimal amountChange;
        private BigDecimal newTotal;
        private Integer status;
        private LocalDateTime createdAt;
    }
}
