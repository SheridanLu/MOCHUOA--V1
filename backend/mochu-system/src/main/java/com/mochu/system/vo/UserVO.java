package com.mochu.system.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Long deptId;
    private String deptName;
    private Integer status;
    private List<RoleVO> roles;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
