package com.mochu.business.hr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_hr_employee")
public class BizHrEmployee extends BaseEntity {
    private Long userId;
    private String employeeNo;
    private String realName;
    private Integer gender;
    private LocalDate birthDate;
    private String idCard;
    private String phone;
    private String address;
    private LocalDate entryDate;
    private LocalDate leaveDate;
    private Integer status;
    private Long deptId;
    private String position;
}
