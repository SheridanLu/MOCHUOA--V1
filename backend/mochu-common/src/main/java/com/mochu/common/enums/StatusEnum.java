package com.mochu.common.enums;

import lombok.Getter;

@Getter
public enum StatusEnum {
    ENABLED(1, "启用"),
    DISABLED(0, "停用");

    private final int value;
    private final String desc;

    StatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
