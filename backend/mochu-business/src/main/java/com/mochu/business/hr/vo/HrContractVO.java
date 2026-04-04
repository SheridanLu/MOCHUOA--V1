package com.mochu.business.hr.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class HrContractVO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Integer contractType;
    private String contractTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String fileUrl;
    private Integer status;
    private String statusName;
    private Long daysToExpire;
}
