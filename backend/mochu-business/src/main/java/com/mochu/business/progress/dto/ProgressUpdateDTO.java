package com.mochu.business.progress.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProgressUpdateDTO {
    // Single node update
    private Long taskId;
    private BigDecimal progress;
    private LocalDate actualStart;
    private LocalDate actualEnd;
    private String description;
    // Batch update
    private List<BatchItem> batchItems;

    @Data
    public static class BatchItem {
        @NotNull private Long taskId;
        private BigDecimal progress;
        private String description;
    }
}
