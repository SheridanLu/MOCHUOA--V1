package com.mochu.business.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_finance_income_split")
public class BizFinanceIncomeSplit extends BaseEntity {
    private Long projectId;
    private Long contractId;
    private String splitNo;
    private String period;
    private BigDecimal amount;
    private Integer status;
    private Long creatorId;
}
