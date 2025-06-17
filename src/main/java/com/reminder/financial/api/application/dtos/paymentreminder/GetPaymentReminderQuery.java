package com.reminder.financial.api.application.dtos.paymentreminder;

import jakarta.validation.constraints.NotBlank;

public record GetPaymentReminderQuery(
        @NotBlank
        String id,
        @NotBlank
        String email
) {
}
