package com.mochu.business.hr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class HrContractCreateDTO {
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;
    @NotNull(message = "合同类型不能为空")
    private Integer contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String fileUrl;
}
