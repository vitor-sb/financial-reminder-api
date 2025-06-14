package com.project.manager.finance.management.application.dtos.paymentreminder;

import jakarta.validation.constraints.NotBlank;

public record GetPaymentReminderQuery(
        @NotBlank
        String id,
        @NotBlank
        String email
) {
}
