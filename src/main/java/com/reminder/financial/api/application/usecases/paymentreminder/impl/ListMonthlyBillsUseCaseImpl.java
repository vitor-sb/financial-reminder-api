package com.reminder.financial.api.application.usecases.paymentreminder.impl;

import com.reminder.financial.api.application.dtos.paymentreminder.ListMonthlyBillsResult;
import com.reminder.financial.api.application.dtos.paymentreminder.MonthlyBillsSummaryResult;
import com.reminder.financial.api.application.usecases.paymentreminder.ListMonthlyBillsUseCase;
import com.reminder.financial.api.application.usecases.paymentreminder.monthlybills.MonthlyBillsPaginator;
import com.reminder.financial.api.application.usecases.paymentreminder.monthlybills.MonthlyBillsSimulator;
import com.reminder.financial.api.application.usecases.paymentreminder.monthlybills.MonthlyBillsTotals;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ListMonthlyBillsUseCaseImpl implements ListMonthlyBillsUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ListMonthlyBillsUseCaseImpl.class);
    private static final int MAX_YEARS_IN_FUTURE_ALLOWED = 5;

    private final PaymentTransactionService transactionService;

    private final PaymentReminderService reminderService;


    @Override
    public MonthlyBillsSummaryResult execute(String userEmail, Integer year, Integer month, boolean withSimulatedBills, Pageable pageable) {
        logger.info("Executing ListMonthlyBills | Period: {}/{} | Simulated: {}", month, year, withSimulatedBills);
        YearMonth queryYearMonth = prepareAndValidateQueryDate(year, month);

        List<ListMonthlyBillsResult> allBills = fetchAndCombineBills(userEmail, queryYearMonth, withSimulatedBills);

        MonthlyBillsTotals totals = MonthlyBillsTotals.of(allBills);

        Page<ListMonthlyBillsResult> billsPage = MonthlyBillsPaginator.paginate(allBills, pageable);

        logger.info("Monthly bills summary generated. Total items: {}", allBills.size());

        return new MonthlyBillsSummaryResult(billsPage, totals.paid(), totals.pending(), totals.overdue());
    }

    private YearMonth prepareAndValidateQueryDate(Integer year, Integer month) {
        YearMonth now = YearMonth.now();
        YearMonth queryYearMonth = YearMonth.of(
                year != null ? year : now.getYear(),
                month != null ? month : now.getMonthValue()
        );

        YearMonth limitDate = now.plusYears(MAX_YEARS_IN_FUTURE_ALLOWED);
        if (queryYearMonth.isAfter(limitDate)) {
            logger.warn("Validation failed: Query date {} is beyond the limit of {} years", queryYearMonth, MAX_YEARS_IN_FUTURE_ALLOWED);
            throw new IllegalArgumentException(
                    "Date exceeds the allowed limit. You can only query up to " + MAX_YEARS_IN_FUTURE_ALLOWED + " years in the future."
            );
        }
        return queryYearMonth;
    }

    private List<ListMonthlyBillsResult> fetchAndCombineBills(String userEmail, YearMonth yearMonth, boolean withSimulatedBills) {
        List<ListMonthlyBillsResult> realBills = findRealBillsForMonth(userEmail, yearMonth);
        logger.debug("Found {} real transactions for {}", realBills.size(), yearMonth);

        List<ListMonthlyBillsResult> simulatedBills = new ArrayList<>();
        if (withSimulatedBills) {
            simulatedBills = simulateFutureBillsForMonth(userEmail, yearMonth);
            logger.debug("Generated {} simulated bills for {}", simulatedBills.size(), yearMonth);
        }

        return mergeAndSortBills(realBills, simulatedBills);
    }

    private List<ListMonthlyBillsResult> findRealBillsForMonth(String userEmail, YearMonth yearMonth) {
        return transactionService.findTransactionsForMonth(userEmail, yearMonth).stream()
                .map(this::mapTransactionToResult)
                .collect(Collectors.toList());
    }

    private List<ListMonthlyBillsResult> simulateFutureBillsForMonth(String userEmail, YearMonth yearMonth) {
        return reminderService.getRelevantRemindersForSimulation(userEmail, yearMonth).stream()
                .flatMap(reminder -> MonthlyBillsSimulator.simulate(reminder, yearMonth).stream())
                .collect(Collectors.toList());
    }

    private String generateBillKey(ListMonthlyBillsResult bill) {
        return String.format("%s-%s", bill.dueDate(), bill.reminderId());
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
}
