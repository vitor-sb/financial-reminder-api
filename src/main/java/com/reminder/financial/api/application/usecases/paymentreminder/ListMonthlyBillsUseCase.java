package com.reminder.financial.api.application.usecases.paymentreminder;

import com.reminder.financial.api.application.dtos.paymentreminder.ListMonthlyBillsResult;

import java.time.YearMonth;
import java.util.List;

public interface ListMonthlyBillsUseCase {
    List<ListMonthlyBillsResult> execute(String userEmail, YearMonth yearMonth);

}
