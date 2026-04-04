package com.mochu.business.project.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_project_document")
public class BizProjectDocument extends BaseEntity {
    private Long projectId;
    private String docName;
    private String docType;
    private String fileUrl;
    private Long uploaderId;
}
