package com.mochu.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 统一分页响应
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> records;
    private long total;
    private int page;
    private int size;
    private int pages;

    public PageResult(List<T> records, long total, int page, int size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = (int) Math.ceil((double) total / size);
    }
}
