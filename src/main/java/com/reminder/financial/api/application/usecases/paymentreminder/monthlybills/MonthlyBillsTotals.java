package com.reminder.financial.api.application.usecases.paymentreminder.monthlybills;

import com.reminder.financial.api.application.dtos.paymentreminder.ListMonthlyBillsResult;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record MonthlyBillsTotals(BigDecimal paid, BigDecimal pending, BigDecimal overdue) {

    public static MonthlyBillsTotals of(List<ListMonthlyBillsResult> bills) {
        Map<TransactionStatusEnum, BigDecimal> byStatus = bills.stream()
                .collect(Collectors.groupingBy(
                        ListMonthlyBillsResult::status,
                        Collectors.reducing(BigDecimal.ZERO, ListMonthlyBillsResult::value, BigDecimal::add)
                ));

        return new MonthlyBillsTotals(
                byStatus.getOrDefault(TransactionStatusEnum.PAGO, BigDecimal.ZERO),
                byStatus.getOrDefault(TransactionStatusEnum.PENDENTE, BigDecimal.ZERO),
                byStatus.getOrDefault(TransactionStatusEnum.VENCIDO, BigDecimal.ZERO)
        );
    }
}
