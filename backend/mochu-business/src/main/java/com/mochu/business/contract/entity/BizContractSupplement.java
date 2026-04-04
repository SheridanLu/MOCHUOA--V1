package com.mochu.business.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_contract_supplement")
public class BizContractSupplement extends BaseEntity {
    private Long contractId;
    private String supplementNo;
    private String reason;
    private BigDecimal amountChange;
    private BigDecimal newTotal;
    private String fileUrl;
    private Integer status;
    private Long approverId;
    private LocalDateTime approvedAt;
}
