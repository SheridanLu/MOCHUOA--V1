package com.mochu.business.completion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_completion_report")
public class BizCompletionReport extends BaseEntity {
    private Long projectId;
    private String reportNo;
    private LocalDate completionDate;
    private Integer qualityRating;
    private String summary;
    private String fileUrl;
    private Integer status;
    private Long creatorId;
    private Long approverId;
    private LocalDateTime approvedAt;
}
