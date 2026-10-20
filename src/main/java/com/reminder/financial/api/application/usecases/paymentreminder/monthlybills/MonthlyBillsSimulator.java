package com.reminder.financial.api.application.usecases.paymentreminder.monthlybills;

import com.reminder.financial.api.application.dtos.paymentreminder.ListMonthlyBillsResult;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public final class MonthlyBillsSimulator {

    private MonthlyBillsSimulator() {}

    public static List<ListMonthlyBillsResult> simulate(PaymentReminder reminder, YearMonth targetMonth) {
        List<ListMonthlyBillsResult> bills = new ArrayList<>();
        RecurrenceEnum recurrence = reminder.getRecurrence();

        LocalDate nextDate = recurrence.calculateSimulationStartDate(reminder.getBaseDate(), targetMonth);

        while (YearMonth.from(nextDate).equals(targetMonth)) {
            int counter = (int) recurrence.calculateInstallmentsBetween(reminder.getBaseDate(), nextDate) + 1;

            if (reminder.getInstallmentsNumber() != null && counter > reminder.getInstallmentsNumber()) {
                break;
            }

            Integer installment = reminder.getInstallmentsNumber() == null ? null : counter;
            bills.add(toResult(reminder, nextDate, installment));
            nextDate = recurrence.getNextDate(nextDate);
        }

        return bills;
    }

    private static ListMonthlyBillsResult toResult(PaymentReminder reminder, LocalDate dueDate, Integer installment) {
        return new ListMonthlyBillsResult(
                null,
                reminder.getId(),
                reminder.getTitle(),
                dueDate,
                reminder.getBaseValue(),
                reminder.getMethod(),
                TransactionStatusEnum.PENDENTE,
                installment
        );
    }
}
