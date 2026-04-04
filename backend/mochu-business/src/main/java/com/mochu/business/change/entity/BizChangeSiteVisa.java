package com.mochu.business.change.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_change_site_visa")
public class BizChangeSiteVisa extends BaseEntity {
    private Long projectId;
    private String visaNo;
    private String description;
    private BigDecimal amount;
    private Integer status;
    private String fileUrl;
    private Long creatorId;
    private Long approverId;
    private LocalDateTime approvedAt;
}
