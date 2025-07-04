package com.reminder.financial.api.adapters.dtos.paymentremider;

import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;

import java.time.LocalDate;

public record ListPaymentRemindersFilterRequest(
        String reminderId,
        String title,
        LocalDate dueDateFrom,
        LocalDate dueDateUntil,
        RecurrenceEnum recurrence,
        TransactionMethodEnum method,
        Boolean enabled
) {
}
