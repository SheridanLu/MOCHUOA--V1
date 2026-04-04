package com.mochu.business.change.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_change_owner")
public class BizChangeOwner extends BaseEntity {
    private Long projectId;
    private String changeNo;
    private String description;
    private BigDecimal amountChange;
    private Integer status;
    private String fileUrl;
    private Long creatorId;
    private Long approverId;
    private LocalDateTime approvedAt;
}
