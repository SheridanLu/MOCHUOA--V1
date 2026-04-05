package com.mochu.business.completion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_completion_settlement")
public class BizCompletionSettlement extends BaseEntity {
    private Long projectId;
    private String settlementNo;
    private BigDecimal contractAmount;
    private BigDecimal changeAmount;
    private BigDecimal finalAmount;
    private Integer status;
    private Long creatorId;
    private Long approverId;
    private LocalDateTime approvedAt;
}
