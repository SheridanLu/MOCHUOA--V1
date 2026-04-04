package com.mochu.business.progress.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_progress_plan")
public class BizProgressPlan extends BaseEntity {
    private Long projectId;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private Long creatorId;
}
