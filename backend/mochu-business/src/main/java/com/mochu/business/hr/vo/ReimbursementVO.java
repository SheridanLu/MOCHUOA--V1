package com.mochu.business.hr.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReimbursementVO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long projectId;
    private String projectName;
    private String reimburseNo;
    private String category;
    private BigDecimal amount;
    private String description;
    private Integer status;
    private String statusName;
    private String fileUrl;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
