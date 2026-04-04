package com.mochu.business.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_finance_cost")
public class BizFinanceCost extends BaseEntity {
    private Long projectId;
    private Integer costType;
    private String category;
    private BigDecimal amount;
    private String description;
    private String period;
}
