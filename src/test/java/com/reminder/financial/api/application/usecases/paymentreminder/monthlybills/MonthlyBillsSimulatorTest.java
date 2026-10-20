package com.reminder.financial.api.application.usecases.paymentreminder.monthlybills;

import com.reminder.financial.api.application.dtos.paymentreminder.ListMonthlyBillsResult;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonthlyBillsSimulatorTest {

    private PaymentReminder reminder(RecurrenceEnum recurrence, Integer installments) {
        PaymentReminder r = new PaymentReminder();
        r.setRecurrence(recurrence);
        r.setInstallmentsNumber(installments);
        r.setBaseDate(LocalDate.of(2026, 1, 10));
        r.setBaseValue(BigDecimal.TEN);
        return r;
    }

    @Test
    void monthlyGeneratesOnePendingBillInTargetMonth() {
        List<ListMonthlyBillsResult> bills = MonthlyBillsSimulator.simulate(reminder(RecurrenceEnum.MENSAL, null), YearMonth.of(2026, 3));
        assertEquals(1, bills.size());
        assertEquals(LocalDate.of(2026, 3, 10), bills.get(0).dueDate());
        assertEquals(TransactionStatusEnum.PENDENTE, bills.get(0).status());
        assertNull(bills.get(0).installmentNumber());
    }

    @Test
    void weeklyGeneratesSeveralBills() {
        assertTrue(MonthlyBillsSimulator.simulate(reminder(RecurrenceEnum.SEMANAL, null), YearMonth.of(2026, 3)).size() >= 4);
    }

    @Test
    void stopsAfterLastInstallment() {
        assertTrue(MonthlyBillsSimulator.simulate(reminder(RecurrenceEnum.MENSAL, 2), YearMonth.of(2026, 3)).isEmpty());
        assertEquals(2, MonthlyBillsSimulator.simulate(reminder(RecurrenceEnum.MENSAL, 2), YearMonth.of(2026, 2)).get(0).installmentNumber());
    }
}
