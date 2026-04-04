package com.mochu.business.change.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_change_labor_visa")
public class BizChangeLaborVisa extends BaseEntity {
    private Long projectId;
    private String visaNo;
    private String description;
    private Integer laborCount;
    private BigDecimal amount;
    private Integer status;
    private Long creatorId;
    private Long approverId;
    private LocalDateTime approvedAt;
}
