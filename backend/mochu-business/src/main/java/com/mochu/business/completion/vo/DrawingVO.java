package com.mochu.business.completion.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DrawingVO {
    private Long id;
    private Long projectId;
    private String drawingName;
    private String drawingType;
    private String fileUrl;
    private Integer version;
    private String uploaderName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
