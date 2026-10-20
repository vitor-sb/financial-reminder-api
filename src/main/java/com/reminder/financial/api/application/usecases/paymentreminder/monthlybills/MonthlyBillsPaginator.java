package com.reminder.financial.api.application.usecases.paymentreminder.monthlybills;

import com.reminder.financial.api.application.dtos.paymentreminder.ListMonthlyBillsResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

public final class MonthlyBillsPaginator {

    private MonthlyBillsPaginator() {}

    public static Page<ListMonthlyBillsResult> paginate(List<ListMonthlyBillsResult> bills, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start > bills.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, bills.size());
        }
        int end = Math.min(start + pageable.getPageSize(), bills.size());
        return new PageImpl<>(bills.subList(start, end), pageable, bills.size());
    }
}
