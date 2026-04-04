package com.mochu.business.contact.vo;

import lombok.Data;

@Data
public class ContactVO {
    private Long id;
    private Long userId;
    private Long deptId;
    private String deptName;
    private String realName;
    private String phone;
    private String email;
    private String position;
}
