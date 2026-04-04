package com.mochu.business.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_material_inbound")
public class BizMaterialInbound extends BaseEntity {
    private String inboundNo;
    private Long contractId;
    private Long projectId;
    private Long supplierId;
    private String warehouse;
    private String receiver;
    private LocalDate inboundDate;
    private Integer status;
    private BigDecimal totalAmount;
    private Long creatorId;
}
