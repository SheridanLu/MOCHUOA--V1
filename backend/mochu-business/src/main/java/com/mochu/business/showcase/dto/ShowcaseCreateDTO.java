package com.mochu.business.showcase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ShowcaseCreateDTO {
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;
    private String coverUrl;
    private String videoUrl;
    private String panoramaUrl;
    private Integer visibility;
    private List<ImageItem> images;

    @Data
    public static class ImageItem {
        private String imageUrl;
        private String caption;
        private Integer sortOrder;
    }
}
