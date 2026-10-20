package com.reminder.financial.api.adapters.in.web.dtos.paymentreminder;

import com.reminder.financial.api.domains.models.reminder.enums.RecurrenceEnum;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionMethodEnum;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
        @Positive @Digits(integer = 12, fraction = 2)
        BigDecimal value,
        @Min(1) @Max(600)
        Integer installmentsNumber
) {
}
