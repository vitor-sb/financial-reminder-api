package com.reminder.financial.api.adapters.dtos.paymentremider;

import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePaymentReminderRequest(
        @NotBlank
        @Size(max=30)
        String title,
        @Size(max=100)
        String description,
        @NotNull
        String date,
        @NotNull
        RecurrenceEnum recurrence,
        @NotNull
        TransactionMethodEnum method,
        @NotNull
        BigDecimal value,
        Integer installmentsNumber
) {
}
