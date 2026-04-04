package com.mochu.business.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_purchase_list")
public class BizPurchaseList extends BaseEntity {
    private Long projectId;
    private Long contractId;
    private String listNo;
    private Integer status;
    private BigDecimal totalAmount;
    private Long creatorId;
    private LocalDateTime approvedAt;
    private Long approverId;
}
