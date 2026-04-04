package com.mochu.system.vo;

import lombok.Data;

@Data
public class CheckAccountVO {
    private boolean exists;
    private String loginType;
    private String maskedPhone;
}
