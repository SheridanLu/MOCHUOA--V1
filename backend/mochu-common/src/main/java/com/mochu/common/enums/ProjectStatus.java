package com.mochu.common.enums;

import lombok.Getter;

@Getter
public enum ProjectStatus {
    DRAFT(1, "草稿"),
    PENDING(2, "待审批"),
    APPROVED(3, "已审批"),
    IN_PROGRESS(4, "进行中"),
    PAUSED(5, "已暂停"),
    CLOSED(6, "已关闭"),
    TERMINATED(7, "已中止"),
    TRACKING(8, "跟踪中"),
    CONVERTED(9, "已转实体");

    private final int value;
    private final String desc;

    ProjectStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
