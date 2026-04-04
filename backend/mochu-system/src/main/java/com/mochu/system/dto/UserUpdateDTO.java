package com.mochu.system.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class UserUpdateDTO {
    @NotBlank(message = "姓名不能为空")
    private String realName;

    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    private String email;
    private Long deptId;
    private List<Long> roleIds;
}
