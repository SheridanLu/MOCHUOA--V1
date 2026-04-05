package com.mochu.business.announcement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnnouncementCreateDTO {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotNull(message = "类型不能为空")
    private Integer type;

    private Integer pinned;

    private LocalDateTime publishAt;

    private List<Long> deptIds;
}
