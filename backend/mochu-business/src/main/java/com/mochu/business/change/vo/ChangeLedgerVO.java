package com.mochu.business.change.vo;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChangeLedgerVO {
    private Long id;
    private String changeType;
    private String changeTypeName;
    private Long projectId;
    private String projectName;
    private String changeNo;
    private String description;
    private BigDecimal amount;
    private Integer status;
    private String statusName;
    private LocalDateTime createdAt;
}
