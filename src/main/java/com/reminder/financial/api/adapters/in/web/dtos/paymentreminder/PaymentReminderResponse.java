package com.reminder.financial.api.adapters.in.web.dtos.paymentreminder;

import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;

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
