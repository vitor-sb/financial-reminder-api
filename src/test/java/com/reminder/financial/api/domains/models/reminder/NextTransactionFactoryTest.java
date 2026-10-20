package com.reminder.financial.api.domains.models.reminder;

import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NextTransactionFactoryTest {

    private static final LocalDate DUE = LocalDate.of(2026, 1, 31);

    private PaymentTransaction current(RecurrenceEnum recurrence, Integer installments, Integer currentInstallment) {
        PaymentReminder reminder = new PaymentReminder();
        reminder.setRecurrence(recurrence);
        reminder.setInstallmentsNumber(installments);
        reminder.setBaseValue(BigDecimal.TEN);
        return new PaymentTransaction(reminder, DUE, null, currentInstallment, BigDecimal.TEN, TransactionStatusEnum.PAGO);
    }

    @Test
    void unicaHasNoNext() {
        assertTrue(NextTransactionFactory.createNext(current(RecurrenceEnum.UNICA, null, null)).isEmpty());
    }

    @Test
    void exhaustedInstallmentsHaveNoNext() {
        assertTrue(NextTransactionFactory.createNext(current(RecurrenceEnum.MENSAL, 3, 3)).isEmpty());
    }

    @Test
    void nullInstallmentKeepsNullAndGeneratesNext() {
        PaymentTransaction next = NextTransactionFactory.createNext(current(RecurrenceEnum.MENSAL, 3, null)).orElseThrow();
        assertNull(next.getInstallmentNumber());
        assertEquals(TransactionStatusEnum.PENDENTE, next.getStatus());
        assertEquals(BigDecimal.TEN, next.getPaymentValue());
    }

    @Test
    void incrementsInstallment() {
        assertEquals(2, NextTransactionFactory.createNext(current(RecurrenceEnum.MENSAL, 3, 1)).orElseThrow().getInstallmentNumber());
    }

    @Test
    void eachRecurrenceAdvancesDate() {
        for (RecurrenceEnum r : RecurrenceEnum.values()) {
            if (r == RecurrenceEnum.UNICA) continue;
            Optional<PaymentTransaction> next = NextTransactionFactory.createNext(current(r, null, null));
            assertEquals(r.getNextDate(DUE), next.orElseThrow().getDueDate(), r.name());
        }
    }
}
