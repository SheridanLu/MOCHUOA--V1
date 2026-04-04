package com.mochu.business.hr.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeVO {
    private Long id;
    private Long userId;
    private String employeeNo;
    private String realName;
    private Integer gender;
    private String genderName;
    private LocalDate birthDate;
    private String idCard;
    private String phone;
    private String address;
    private LocalDate entryDate;
    private LocalDate leaveDate;
    private Integer status;
    private String statusName;
    private Long deptId;
    private String deptName;
    private String position;
    private LocalDateTime createdAt;
}
