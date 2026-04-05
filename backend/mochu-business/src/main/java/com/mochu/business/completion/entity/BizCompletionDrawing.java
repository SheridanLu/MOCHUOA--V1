package com.mochu.business.completion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_completion_drawing")
public class BizCompletionDrawing extends BaseEntity {
    private Long projectId;
    private String drawingName;
    private String drawingType;
    private String fileUrl;
    private Integer version;
    private Long uploaderId;
}
