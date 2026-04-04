package com.mochu.business.change.vo;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OwnerChangeVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private String changeNo;
    private String description;
    private BigDecimal amountChange;
    private Integer status;
    private String statusName;
    private String fileUrl;
    private String creatorName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
