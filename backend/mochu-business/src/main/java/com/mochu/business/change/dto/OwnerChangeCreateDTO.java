package com.mochu.business.change.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class OwnerChangeCreateDTO {
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    private String description;
    private BigDecimal amountChange;
    private String fileUrl;
}
