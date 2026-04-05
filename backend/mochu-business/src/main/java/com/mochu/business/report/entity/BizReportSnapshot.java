package com.mochu.business.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_report_snapshot")
public class BizReportSnapshot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportType;
    private String period;
    private String dataJson;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
}
