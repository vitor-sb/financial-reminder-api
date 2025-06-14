package com.project.manager.finance.management.application.dtos.paymentreminder;

import com.project.manager.finance.management.domains.models.reminder.enums.RecurrenceEnum;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionMethodEnum;

import java.math.BigDecimal;

public record PaymentReminderResult(
        String title,
        String description,
        String date,
        RecurrenceEnum recurrence,
        TransactionMethodEnum method,
        BigDecimal value,
        Integer installmentsNumber
) {
}