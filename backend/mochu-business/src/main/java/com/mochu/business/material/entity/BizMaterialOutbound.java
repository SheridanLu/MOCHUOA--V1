package com.mochu.business.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_material_outbound")
public class BizMaterialOutbound extends BaseEntity {
    private String outboundNo;
    private Long projectId;
    private String warehouse;
    private String recipient;
    private String purpose;
    private LocalDate outboundDate;
    private Integer status;
    private Long creatorId;
}
