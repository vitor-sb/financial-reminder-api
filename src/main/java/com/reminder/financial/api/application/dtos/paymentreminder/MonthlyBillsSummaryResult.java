package com.reminder.financial.api.application.dtos.paymentreminder;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public record MonthlyBillsSummaryResult(
        Page<ListMonthlyBillsResult> bills,
        BigDecimal totalPaid,
        BigDecimal totalPending,
        BigDecimal totalOverdue
) {}
