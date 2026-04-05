package com.mochu.business.completion.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ArchiveVO {
    private Long projectId;
    private String projectName;
    private Map<String, List<ArchiveItem>> categories;

    @Data
    public static class ArchiveItem {
        private String fileName;
        private String fileUrl;
        private String source;
        private String uploadedAt;
    }
}
