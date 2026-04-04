package com.mochu.business.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TemplateCreateDTO {
    @NotBlank(message = "模板名称不能为空")
    private String templateName;
    @NotNull(message = "模板类型不能为空")
    private Integer templateType;
    private String content;
    private String fileUrl;
}
