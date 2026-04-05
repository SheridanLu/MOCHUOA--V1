package com.mochu.business.completion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_completion_labor_settlement")
public class BizCompletionLaborSettlement extends BaseEntity {
    private Long projectId;
    private String settlementNo;
    private String teamName;
    private String laborType;
    private Integer totalDays;
    private BigDecimal dailyRate;
    private BigDecimal totalAmount;
    private BigDecimal deduction;
    private BigDecimal finalAmount;
    private Integer status;
    private Long creatorId;
    private LocalDateTime approvedAt;
}
