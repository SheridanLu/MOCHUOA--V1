package com.mochu.business.hr.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayrollVO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String period;
    private BigDecimal baseSalary;
    private BigDecimal overtime;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal socialInsurance;
    private BigDecimal tax;
    private BigDecimal netSalary;
    private Integer status;
    private String statusName;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
