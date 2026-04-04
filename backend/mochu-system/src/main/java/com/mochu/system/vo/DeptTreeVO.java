package com.mochu.system.vo;

import lombok.Data;
import java.util.List;

@Data
public class DeptTreeVO {
    private Long id;
    private String deptName;
    private Long parentId;
    private String path;
    private Integer sortOrder;
    private Integer status;
    private Long leaderId;
    private List<DeptTreeVO> children;
}
