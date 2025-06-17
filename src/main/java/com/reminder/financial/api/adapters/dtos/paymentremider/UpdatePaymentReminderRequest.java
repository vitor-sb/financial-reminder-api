package com.reminder.financial.api.adapters.dtos.paymentremider;

import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdatePaymentReminderRequest(
        Boolean updatePendingTransaction,
        @Size(max=30)
        String title,
        @Size(max=100)
        String description,
        TransactionMethodEnum method,
        BigDecimal value
) {
}
