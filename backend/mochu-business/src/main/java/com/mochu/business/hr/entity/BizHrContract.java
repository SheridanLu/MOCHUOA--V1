package com.mochu.business.hr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_hr_contract")
public class BizHrContract extends BaseEntity {
    private Long employeeId;
    /** 1=fixed term, 2=indefinite, 3=probation, 4=internship, 5=labor dispatch */
    private Integer contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String fileUrl;
    private Integer status;
}
