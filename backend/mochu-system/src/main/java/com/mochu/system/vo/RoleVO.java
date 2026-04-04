package com.mochu.system.vo;

import lombok.Data;
import java.util.List;

@Data
public class RoleVO {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer dataScope;
    private Integer status;
    private List<Long> permissionIds;
}
