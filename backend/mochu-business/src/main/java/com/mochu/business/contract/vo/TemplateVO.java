package com.mochu.business.contract.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TemplateVO {
    private Long id;
    private String templateName;
    private Integer templateType;
    private String templateTypeName;
    private String content;
    private String fileUrl;
    private Integer version;
    private Integer status;
    private String statusName;
    private LocalDateTime createdAt;
}
