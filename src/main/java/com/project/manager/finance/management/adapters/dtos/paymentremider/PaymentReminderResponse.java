package com.project.manager.finance.management.adapters.dtos.paymentremider;

import com.project.manager.finance.management.domains.models.reminder.enums.RecurrenceEnum;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionMethodEnum;

import java.math.BigDecimal;

public record PaymentReminderResponse(
        String title,
        String description,
        String date,
        RecurrenceEnum recurrence,
        TransactionMethodEnum method,
        BigDecimal value,
        Integer installmentsNumber
) {
}
