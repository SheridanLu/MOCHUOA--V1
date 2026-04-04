package com.mochu.business.hr.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class QualificationVO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String qualName;
    private String qualNo;
    private LocalDate issueDate;
    private LocalDate expireDate;
    private String fileUrl;
    private Long daysToExpire;
}
