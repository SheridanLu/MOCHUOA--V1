package com.mochu.business.progress.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_progress_deviation")
public class BizProgressDeviation {
    private Long id;
    private Long taskId;
    private Long projectId;
    private Integer deviationDays;
    private Integer deviationType;
    private String description;
    private LocalDateTime createdAt;
}
