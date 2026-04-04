package com.mochu.business.project.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mochu.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_project_member")
public class BizProjectMember extends BaseEntity {
    private Long projectId;
    private Long userId;
    private String role;
    private LocalDateTime joinedAt;
}
