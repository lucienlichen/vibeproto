package com.vibeproto.common.model;

import java.util.List;

public record PageResponse<T>(
    Long total,
    Long pageNum,
    Long pageSize,
    List<T> records
) {
}
