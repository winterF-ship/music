package com.musicdemo1.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

public record PageResponse<T>(long current, long size, long total, List<T> records) {
    public static <T> PageResponse<T> from(IPage<T> page) {
        return new PageResponse<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }
}
