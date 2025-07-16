package com.reminder.financial.api.application.dtos.paymentreminder;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdatePaymentReminderCommand(
        @Size(max=30)
        String title,
        @Size(max=100)
        String description,
        TransactionMethodEnum method,
        BigDecimal value
) {
}
