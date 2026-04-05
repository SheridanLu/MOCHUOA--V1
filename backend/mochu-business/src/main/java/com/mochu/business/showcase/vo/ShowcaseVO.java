package com.mochu.business.showcase.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShowcaseVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;
    private String panoramaUrl;
    private Integer visibility;
    private String visibilityName;
    private Integer sortOrder;
    private Integer status;
    private String statusName;
    private Long viewCount;
    private String creatorName;
    private List<ShowcaseImageVO> images;
    private LocalDateTime createdAt;
}
