package com.reminder.financial.api.application.usecases.paymentreminder;

import com.reminder.financial.api.application.dtos.paymentreminder.MonthlyBillsSummaryResult;
import com.reminder.financial.api.domains.pagination.PageRequest;


public interface ListMonthlyBillsUseCase {
    MonthlyBillsSummaryResult execute(String userEmail, Integer year, Integer month, boolean withSimulatedBills, PageRequest pageable);
}
