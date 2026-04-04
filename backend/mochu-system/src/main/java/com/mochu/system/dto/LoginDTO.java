package com.mochu.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message = "账号不能为空")
    private String account;
    private String password;
    private String smsCode;
    private String clientType = "pc";
}
