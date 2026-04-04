package com.mochu.common.enums;

import lombok.Getter;

@Getter
public enum ContractStatus {
    DRAFT(1, "草稿"),
    PENDING(2, "待审批"),
    FIN_APPROVED(3, "财务已审"),
    LEGAL_APPROVED(4, "法务已审"),
    APPROVED(5, "已审批"),
    EXECUTING(6, "执行中"),
    COMPLETED(7, "已完成"),
    TERMINATED(8, "已终止");

    private final int value;
    private final String desc;

    ContractStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
