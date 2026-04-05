package com.mochu.business.completion.dto;

import lombok.Data;
import java.util.List;

@Data
public class DrawingUploadDTO {
    private Long projectId;
    private List<DrawingItem> drawings;

    @Data
    public static class DrawingItem {
        private String drawingName;
        private String drawingType;
        private String fileUrl;
    }
}
