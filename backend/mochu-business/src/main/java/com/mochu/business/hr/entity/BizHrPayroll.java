package com.mochu.business.hr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_hr_payroll")
public class BizHrPayroll extends BaseEntity {
    private Long employeeId;
    private String period;
    private BigDecimal baseSalary;
    private BigDecimal overtime;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal socialInsurance;
    private BigDecimal tax;
    private BigDecimal netSalary;
    private Integer status;
    private LocalDateTime paidAt;
}
