package com.reminder.financial.api.application.usecases.paymentreminder.monthlybills;

import com.reminder.financial.api.application.dtos.paymentreminder.ListMonthlyBillsResult;
import com.reminder.financial.api.domains.pagination.PageRequest;
import com.reminder.financial.api.domains.pagination.PageResult;

import java.util.Collections;
import java.util.List;

public final class MonthlyBillsPaginator {

    private MonthlyBillsPaginator() {}

    public static PageResult<ListMonthlyBillsResult> paginate(List<ListMonthlyBillsResult> bills, PageRequest pageRequest) {
        int start = (int) pageRequest.offset();
        if (start > bills.size()) {
            return new PageResult<>(Collections.emptyList(), pageRequest, bills.size());
        }
        int end = Math.min(start + pageRequest.size(), bills.size());
        return new PageResult<>(bills.subList(start, end), pageRequest, bills.size());
    }
}
