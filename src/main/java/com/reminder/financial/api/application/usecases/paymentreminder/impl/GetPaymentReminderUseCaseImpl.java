package com.reminder.financial.api.application.usecases.paymentreminder.impl;

import com.reminder.financial.api.application.dtos.paymentreminder.GetPaymentReminderQuery;
import com.reminder.financial.api.application.dtos.paymentreminder.PaymentReminderResult;
import com.reminder.financial.api.application.usecases.paymentreminder.GetPaymentReminderUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@RequiredArgsConstructor
@Component
public class GetPaymentReminderUseCaseImpl implements GetPaymentReminderUseCase {

    private final PaymentReminderService paymentReminderService;

    public PaymentReminderResult execute(GetPaymentReminderQuery dto) {
        PaymentReminder paymentReminder = paymentReminderService.getPaymentReminderByIdOrThrow(dto.id());

        if (!Objects.equals(paymentReminder.getUser().getEmail(), dto.email()))
            throw new NotFoundException("Payment Reminder not found.");

        paymentReminder.getAudit().validateIsActive("PaymentReminder");

        return new PaymentReminderResult(
                paymentReminder.getTitle(),
                paymentReminder.getDescription(),
                paymentReminder.getBaseDate().toString(),
                paymentReminder.getRecurrence(),
                paymentReminder.getMethod(),
                paymentReminder.getBaseValue(),
                paymentReminder.getInstallmentsNumber()
        );
    }
}
