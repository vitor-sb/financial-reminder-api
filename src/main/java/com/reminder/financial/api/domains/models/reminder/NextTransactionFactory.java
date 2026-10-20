package com.reminder.financial.api.domains.models.reminder;

import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;

import java.time.LocalDate;
import java.util.Optional;

public final class NextTransactionFactory {

    private NextTransactionFactory() {
    }

    public static Optional<PaymentTransaction> createNext(PaymentTransaction current) {
        PaymentReminder reminder = current.getReminder();
        if (reminder.getRecurrence() == RecurrenceEnum.UNICA) {
            return Optional.empty();
        }

        Integer currentInstallment = current.getInstallmentNumber();
        if (reminder.getInstallmentsNumber() != null && currentInstallment != null
                && currentInstallment >= reminder.getInstallmentsNumber()) {
            return Optional.empty();
        }

        LocalDate nextDueDate = reminder.getRecurrence().getNextDate(current.getDueDate());
        if (nextDueDate == null) {
            return Optional.empty();
        }

        Integer nextInstallment = (currentInstallment != null && currentInstallment != 0) ? currentInstallment + 1 : null;
        return Optional.of(new PaymentTransaction(reminder, nextDueDate, reminder.getMethod(),
                nextInstallment, reminder.getBaseValue(), TransactionStatusEnum.PENDENTE));
    }
}
