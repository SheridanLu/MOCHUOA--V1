package com.mochu.business.project.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_project_payment_batch")
public class BizProjectPaymentBatch extends BaseEntity {
    private Long projectId;
    private Integer batchNo;
    private String description;
    private BigDecimal ratio;
    private BigDecimal amount;
    private LocalDate plannedDate;
    private Integer status;
}
