package com.reminder.financial.api.domains.filters;

import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;

import java.time.LocalDate;

public record ReminderFilter(
        String reminderId,
        String title,
        LocalDate dueDateFrom,
        LocalDate dueDateUntil,
        RecurrenceEnum recurrence,
        TransactionMethodEnum method,
        Boolean enabled
) {
}
