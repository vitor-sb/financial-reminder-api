package com.reminder.financial.api.application.usecases.paymentreminder.monthlybills;

import com.reminder.financial.api.application.dtos.paymentreminder.ListMonthlyBillsResult;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonthlyBillsTotalsTest {

    private ListMonthlyBillsResult bill(TransactionStatusEnum status, String value) {
        return new ListMonthlyBillsResult(null, "1", "t", LocalDate.now(), new BigDecimal(value), null, status, null);
    }

    @Test
    void sumsByStatus() {
        MonthlyBillsTotals t = MonthlyBillsTotals.of(List.of(
                bill(TransactionStatusEnum.PAGO, "10"), bill(TransactionStatusEnum.PAGO, "5"),
                bill(TransactionStatusEnum.PENDENTE, "7")));
        assertEquals(new BigDecimal("15"), t.paid());
        assertEquals(new BigDecimal("7"), t.pending());
        assertEquals(BigDecimal.ZERO, t.overdue());
    }
}
