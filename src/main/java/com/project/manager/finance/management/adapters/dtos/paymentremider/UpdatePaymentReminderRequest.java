package com.project.manager.finance.management.adapters.dtos.paymentremider;

import com.project.manager.finance.management.domains.models.reminder.enums.TransactionMethodEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdatePaymentReminderRequest(
        @NotBlank
        String reminderId,
        Boolean updatePendingTransaction,
        @Size(max=30)
        String title,
        @Size(max=100)
        String description,
        TransactionMethodEnum method,
        BigDecimal value
) {
}
