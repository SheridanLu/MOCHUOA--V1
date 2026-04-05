package com.mochu.business.completion.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CompletionReportDTO {
    private Long projectId;
    private LocalDate completionDate;
    private Integer qualityRating;
    private String summary;
    private String fileUrl;
}
