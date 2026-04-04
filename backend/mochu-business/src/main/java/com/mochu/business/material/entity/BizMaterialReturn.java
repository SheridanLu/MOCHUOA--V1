package com.mochu.business.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_material_return")
public class BizMaterialReturn extends BaseEntity {
    private String returnNo;
    private Long inboundId;
    private Long projectId;
    private LocalDate returnDate;
    private String reason;
    /** 1=draft, 2=pending, 3=approved */
    private Integer status;
    /** 1=on-site, 2=manufacturer, 3=company warehouse, 4=inter-project */
    private Integer returnType;
    private Long targetProjectId;
    private Long creatorId;
}
