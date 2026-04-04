package com.mochu.business.project.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_project")
public class BizProject extends BaseEntity {
    private String projectNo;
    private String projectName;
    private Integer projectType;
    private Integer status;
    private String description;
    private Long ownerId;
    private Long deptId;
    private BigDecimal bidAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal amountWithoutTax;
    private BigDecimal investLimit;
    private String bidNoticeUrl;
    private String terminationReason;
    private Long costTargetProjectId;
    private Long approverId;
    private LocalDateTime approvedAt;
    private LocalDateTime pausedAt;
    private LocalDateTime closedAt;
}
