package com.mochu.business.announcement.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnnouncementVO {
    private Long id;
    private String title;
    private String content;
    private Integer type;
    private String typeName;
    private Integer pinned;
    private LocalDateTime publishAt;
    private String creatorName;
    private Integer status;
    private String statusName;
    private LocalDateTime createdAt;
}
