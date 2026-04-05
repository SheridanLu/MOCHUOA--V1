package com.mochu.system.dto;

import lombok.Data;

@Data
public class AuditLogQueryDTO {
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String targetType;
    private String startDate;
    private String endDate;
    private Integer page = 1;
    private Integer size = 20;
}
