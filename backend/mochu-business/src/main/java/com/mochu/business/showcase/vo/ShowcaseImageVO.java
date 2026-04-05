package com.mochu.business.showcase.vo;

import lombok.Data;

@Data
public class ShowcaseImageVO {
    private Long id;
    private String imageUrl;
    private String caption;
    private Integer sortOrder;
}
