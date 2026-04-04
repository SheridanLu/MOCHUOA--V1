package com.mochu.system.vo;

import lombok.Data;
import java.util.Set;

@Data
public class LoginVO {
    private String token;
    private UserVO userInfo;
    private Set<String> permissions;
}
