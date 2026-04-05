package com.mochu.business.showcase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_showcase_image")
public class BizShowcaseImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long showcaseId;
    private String imageUrl;
    private String caption;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
