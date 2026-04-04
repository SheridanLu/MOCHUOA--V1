package com.mochu.business.announcement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_announcement")
public class SysAnnouncement extends BaseEntity {
    private String title;
    private String content;
    private Integer type;
    private Integer pinned;
    private LocalDateTime publishAt;
    private Long creatorId;
}
