package com.mochu.business.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inventory")
public class BizInventory extends BaseEntity {
    private Long projectId;
    private String materialName;
    private String spec;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal weightedAvgPrice;
    private BigDecimal totalAmount;
    private String warehouse;
}
