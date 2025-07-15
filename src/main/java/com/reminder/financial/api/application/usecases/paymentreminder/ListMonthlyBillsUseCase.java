package com.reminder.financial.api.application.usecases.paymentreminder;

import com.reminder.financial.api.application.dtos.paymentreminder.MonthlyBillsSummaryResult;
import org.springframework.data.domain.Pageable;

import java.time.YearMonth;

public interface ListMonthlyBillsUseCase {
    MonthlyBillsSummaryResult execute(String userEmail, Integer year, Integer month, boolean withSimulatedBills, Pageable pageable);
}
