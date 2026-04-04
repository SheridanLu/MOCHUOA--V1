package com.mochu.business.change.dto;
import lombok.Data;

@Data
public class ChangeQueryDTO {
    private Long projectId;
    private Integer status;
    private String changeType;
    private Integer page = 1;
    private Integer size = 20;
}
