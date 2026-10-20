package com.reminder.financial.api.application.dtos.paymentreminder;

import com.reminder.financial.api.domains.pagination.PageResult;

import java.math.BigDecimal;

public record MonthlyBillsSummaryResult(
        PageResult<ListMonthlyBillsResult> bills,
        BigDecimal totalPaid,
        BigDecimal totalPending,
        BigDecimal totalOverdue
) {}
