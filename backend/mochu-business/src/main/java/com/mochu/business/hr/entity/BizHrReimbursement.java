package com.mochu.business.hr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_hr_reimbursement")
public class BizHrReimbursement extends BaseEntity {
    private Long employeeId;
    private Long projectId;
    private String reimburseNo;
    private String category;
    private BigDecimal amount;
    private String description;
    private Integer status;
    private String fileUrl;
    private Long approverId;
    private LocalDateTime approvedAt;
    private Long creatorId;
}
