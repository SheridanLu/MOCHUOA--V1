package com.mochu.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckAccountDTO {
    @NotBlank(message = "账号不能为空")
    private String account;
}
