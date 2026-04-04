package com.mochu.business.showcase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_showcase")
public class BizShowcase extends BaseEntity {
    private Long projectId;
    private String title;
    private String description;
    private String coverUrl;
    private Integer sortOrder;
    private Integer status;
    private Long creatorId;
}
