package com.mochu.business.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeCreateDTO {
    private Long userId;
    @NotBlank(message = "姓名不能为空")
    private String realName;
    private Integer gender;
    private LocalDate birthDate;
    private String idCard;
    private String phone;
    private String address;
    @NotNull(message = "入职日期不能为空")
    private LocalDate entryDate;
    @NotNull(message = "部门不能为空")
    private Long deptId;
    private String position;
}
