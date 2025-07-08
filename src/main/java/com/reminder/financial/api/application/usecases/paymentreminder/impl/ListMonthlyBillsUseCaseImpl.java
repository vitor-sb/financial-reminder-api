package com.reminder.financial.api.application.usecases.paymentreminder.impl;

import com.reminder.financial.api.application.dtos.paymentreminder.ListMonthlyBillsResult;
import com.reminder.financial.api.application.usecases.paymentreminder.ListMonthlyBillsUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ListMonthlyBillsUseCaseImpl implements ListMonthlyBillsUseCase {

    @Autowired
    private PaymentTransactionService transactionService;

    @Autowired
    private PaymentReminderService reminderService;

    @Override
    public List<ListMonthlyBillsResult> execute(String userEmail, YearMonth yearMonth) {
        List<ListMonthlyBillsResult> realBills = findRealBillsForMonth(userEmail, yearMonth);
        List<ListMonthlyBillsResult> simulatedBills = simulateFutureBillsForMonth(userEmail, yearMonth);

        return mergeAndSortBills(realBills, simulatedBills);
    }

    private List<ListMonthlyBillsResult> mergeAndSortBills(
            List<ListMonthlyBillsResult> real,
            List<ListMonthlyBillsResult> simulated
    ) {
        Map<String, ListMonthlyBillsResult> billMap = new LinkedHashMap<>();

        real.forEach(bill -> billMap.put(generateBillKey(bill), bill));

        simulated.forEach(bill -> billMap.putIfAbsent(generateBillKey(bill), bill));

        return billMap.values().stream()
                .sorted(Comparator.comparing(ListMonthlyBillsResult::dueDate))
                .collect(Collectors.toList());
    }

    private String generateBillKey(ListMonthlyBillsResult bill) {
        return String.format("%s-%s", bill.dueDate(), bill.reminderId());
    }

    private List<ListMonthlyBillsResult> findRealBillsForMonth(String userEmail, YearMonth yearMonth) {
        return transactionService.findTransactionsForMonth(userEmail, yearMonth).stream()
                .map(this::mapTransactionToResult)
                .collect(Collectors.toList());
    }

    private List<ListMonthlyBillsResult> simulateFutureBillsForMonth(String userEmail, YearMonth yearMonth) {
        return reminderService.listPaymentRemindersByEmail(userEmail).stream()
                .filter(reminder -> reminder.getRecurrence() != RecurrenceEnum.UNICA)
                .flatMap(reminder -> generateSimulatedBillsForReminder(reminder, yearMonth).stream())
                .collect(Collectors.toList());
    }

    private List<ListMonthlyBillsResult> generateSimulatedBillsForReminder(
            PaymentReminder reminder,
            YearMonth targetMonth
    ) {
        List<ListMonthlyBillsResult> bills = new ArrayList<>();
        RecurrenceEnum recurrence = reminder.getRecurrence();

        LocalDate nextDate = recurrence.calculateSimulationStartDate(reminder.getBaseDate(), targetMonth);

        while (YearMonth.from(nextDate).equals(targetMonth)) {
            long installments = recurrence.calculateInstallmentsBetween(reminder.getBaseDate(), nextDate);
            int currentInstallment = (int) installments + 1;

            if (reminder.getInstallmentsNumber() > 0 && currentInstallment > reminder.getInstallmentsNumber()) {
                break;
            }

            bills.add(mapReminderToSimulatedResult(reminder, nextDate, currentInstallment));

            nextDate = recurrence.getNextDate(nextDate);
        }

        return bills;
    }

    private ListMonthlyBillsResult mapTransactionToResult(PaymentTransaction transaction) {
        return new ListMonthlyBillsResult(
                transaction.getId(),
                transaction.getReminder().getId(),
                transaction.getReminder().getTitle(),
                transaction.getDueDate(),
                transaction.getPaymentValue(),
                transaction.getMethod(),
                transaction.getStatus(),
                transaction.getInstallmentNumber()
        );
    }

    private ListMonthlyBillsResult mapReminderToSimulatedResult(
            PaymentReminder reminder,
            LocalDate dueDate,
            int installmentNumber
    ) {
        return new ListMonthlyBillsResult(
                null,
                reminder.getId(),
                reminder.getTitle(),
                dueDate,
                reminder.getBaseValue(),
                reminder.getMethod(),
                TransactionStatusEnum.PENDENTE,
                installmentNumber
        );
    }
}