package com.mochu.business.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_benchmark_price")
public class BizBenchmarkPrice extends BaseEntity {
    private String materialName;
    private String spec;
    private String unit;
    private BigDecimal benchmarkPrice;
    /** 1=auto(from contract), 2=manual */
    private Integer updateType;
    private Long sourceContractId;
    private Long sourceSupplierId;
    private Integer status;
    private Long creatorId;
}
