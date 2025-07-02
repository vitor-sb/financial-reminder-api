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
import java.time.temporal.ChronoUnit;
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
        return bill.dueDate().toString() + "-" + bill.reminderId();
    }

    private List<ListMonthlyBillsResult> findRealBillsForMonth(String userEmail, YearMonth yearMonth) {
        return transactionService.findTransactionsForMonth(userEmail, yearMonth).stream()
                .map(this::mapTransactionToResult)
                .collect(Collectors.toList());
    }

    private List<ListMonthlyBillsResult> simulateFutureBillsForMonth(String userEmail, YearMonth yearMonth) {
        return reminderService.listPaymentRemindersByEmail(userEmail).stream()
                .filter(reminder -> reminder.getRecurrence() != RecurrenceEnum.UNICA)
                .filter(reminder -> Boolean.TRUE.equals(reminder.getAudit().getEnabled()))
                .flatMap(reminder -> generateSimulatedBillsForReminder(reminder, yearMonth).stream())
                .collect(Collectors.toList());
    }

    private List<ListMonthlyBillsResult> generateSimulatedBillsForReminder(
            PaymentReminder reminder,
            YearMonth targetMonth
    ) {
        List<ListMonthlyBillsResult> bills = new ArrayList<>();

        LocalDate startDate = calculateAdjustedStartDate(reminder, targetMonth);
        if (YearMonth.from(startDate).isAfter(targetMonth)) {
            return bills;
        }

        int installment = calculateCurrentInstallment(reminder.getBaseDate(), startDate, reminder.getRecurrence());
        LocalDate nextDate = startDate;

        while (nextDate != null && YearMonth.from(nextDate).isBefore(targetMonth.plusMonths(1))) {
            if (reminder.getInstallmentsNumber() > 0 && installment > reminder.getInstallmentsNumber()) {
                break;
            }

            if (YearMonth.from(nextDate).equals(targetMonth)) {
                bills.add(mapReminderToSimulatedResult(reminder, nextDate, installment));
            }

            nextDate = reminder.getRecurrence().getNextDate(nextDate);
            installment++;
        }
        return bills;
    }

    private LocalDate calculateAdjustedStartDate(
            PaymentReminder reminder,
            YearMonth targetMonth
    ) {
        LocalDate baseDate = reminder.getBaseDate();
        if (YearMonth.from(baseDate).isAfter(targetMonth)) {
            return baseDate;
        }

        long diff;
        return switch (reminder.getRecurrence()) {
            case DIARIA -> {
                diff = ChronoUnit.DAYS.between(baseDate, targetMonth.atDay(1));
                yield baseDate.plusDays(diff);
            }
            case SEMANAL -> {
                diff = ChronoUnit.WEEKS.between(baseDate, targetMonth.atDay(1));
                yield baseDate.plusWeeks(diff);
            }
            case QUINZENAL -> {
                diff = ChronoUnit.WEEKS.between(baseDate, targetMonth.atDay(1)) / 2;
                yield baseDate.plusWeeks(diff * 2);
            }
            case MENSAL -> {
                diff = ChronoUnit.MONTHS.between(YearMonth.from(baseDate), targetMonth);
                yield baseDate.plusMonths(diff);
            }
            case TRIMESTRAL -> {
                diff = ChronoUnit.MONTHS.between(YearMonth.from(baseDate), targetMonth) / 3;
                yield baseDate.plusMonths(diff * 3);
            }
            case SEMESTRAL -> {
                diff = ChronoUnit.MONTHS.between(YearMonth.from(baseDate), targetMonth) / 6;
                yield baseDate.plusMonths(diff * 6);
            }
            case ANUAL -> {
                diff = ChronoUnit.YEARS.between(YearMonth.from(baseDate), targetMonth);
                yield baseDate.plusYears(diff);
            }
            default -> baseDate;
        };
    }

    private int calculateCurrentInstallment(
            LocalDate baseDate,
            LocalDate startDate,
            RecurrenceEnum recurrence
    ) {
        if (!startDate.isAfter(baseDate)) {
            return 1;
        }
        long diff = switch (recurrence) {
            case DIARIA -> ChronoUnit.DAYS.between(baseDate, startDate);
            case SEMANAL -> ChronoUnit.WEEKS.between(baseDate, startDate);
            case QUINZENAL -> ChronoUnit.WEEKS.between(baseDate, startDate) / 2;
            case MENSAL -> ChronoUnit.MONTHS.between(baseDate, startDate);
            case TRIMESTRAL -> ChronoUnit.MONTHS.between(baseDate, startDate) / 3;
            case SEMESTRAL -> ChronoUnit.MONTHS.between(baseDate, startDate) / 6;
            case ANUAL -> ChronoUnit.YEARS.between(baseDate, startDate);
            default -> 0;
        };
        return (int) diff + 1;
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