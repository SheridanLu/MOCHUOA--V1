package com.mochu.business.contact.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_contact")
public class BizContact extends BaseEntity {
    private Long userId;
    private Long deptId;
    private String realName;
    private String phone;
    private String email;
    private String position;
    private Integer visible;
}
