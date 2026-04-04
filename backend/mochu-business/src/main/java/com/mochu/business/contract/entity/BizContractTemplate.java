package com.mochu.business.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_contract_template")
public class BizContractTemplate extends BaseEntity {
    private String templateName;
    private Integer templateType;
    private String content;
    private String fileUrl;
    private Integer version;
    private Integer status;
    private Long creatorId;
}
