package com.mochu.business.hr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_hr_qualification")
public class BizHrQualification extends BaseEntity {
    private Long employeeId;
    private String qualName;
    private String qualNo;
    private LocalDate issueDate;
    private LocalDate expireDate;
    private String fileUrl;
}
