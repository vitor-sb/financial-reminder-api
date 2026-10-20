package com.reminder.financial.api.adapters.in.web.dtos.paymentreminder;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public record MonthlyBillsSummaryResponse(
        Page<MonthlyBillResponse> bills,
        Totals totals
) {
    public record Totals(
            BigDecimal totalPaid,
            BigDecimal totalPending,
            BigDecimal totalOverdue
    ) {}
}
