package com.reminder.financial.api.adapters.in.web.mappers;

import com.reminder.financial.api.domains.pagination.PageRequest;
import com.reminder.financial.api.domains.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public final class PageAdapter {

    private PageAdapter() {}

    public static PageRequest toRequest(Pageable pageable) {
        return new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().stream().map(o -> new PageRequest.Order(o.getProperty(), o.isAscending())).toList());
    }

    public static <T> Page<T> toPage(PageResult<T> result, Pageable pageable) {
        return new PageImpl<>(result.content(), pageable, result.totalElements());
    }
}
