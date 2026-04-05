package com.mochu.business.announcement.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private String title;
    private String content;
    private String module;
    private Long targetId;
    private Integer readFlag;
    private LocalDateTime createdAt;
}
