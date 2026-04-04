package com.mochu.business.progress.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_progress_task")
public class BizProgressTask extends BaseEntity {
    private Long planId;
    private Long projectId;
    private String taskName;
    private Long parentTaskId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate actualStart;
    private LocalDate actualEnd;
    private BigDecimal progress;
    private Integer status;
    private Long assigneeId;
    private Integer milestone;
    private Integer sortOrder;
}
