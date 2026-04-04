package com.mochu.common.enums;

import lombok.Getter;

@Getter
public enum ApprovalAction {
    APPROVE(1, "同意"),
    REJECT(2, "驳回"),
    TRANSFER(3, "转办");

    private final int value;
    private final String desc;

    ApprovalAction(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
