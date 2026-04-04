package com.mochu.business.material.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReturnVO {
    private Long id;
    private String returnNo;
    private Long inboundId;
    private Long projectId;
    private String projectName;
    private LocalDate returnDate;
    private String reason;
    private Integer status;
    private String statusName;
    private Integer returnType;
    private String returnTypeName;
    private Long targetProjectId;
    private String targetProjectName;
    private LocalDateTime createdAt;
    private List<ReturnItemVO> items;

    @Data
    public static class ReturnItemVO {
        private Long id;
        private Long inboundItemId;
        private String materialName;
        private String spec;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
    }
}
