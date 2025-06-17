package com.reminder.financial.api.application.usecases.paymentreminder.impl;

import com.reminder.financial.api.application.dtos.paymentreminder.GetPaymentReminderQuery;
import com.reminder.financial.api.application.dtos.paymentreminder.PaymentReminderResult;
import com.reminder.financial.api.application.usecases.paymentreminder.GetPaymentReminderUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.application.exceptions.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GetPaymentReminderUseCaseImpl implements GetPaymentReminderUseCase {

    @Autowired
    PaymentReminderService paymentReminderService;

    public PaymentReminderResult execute(GetPaymentReminderQuery dto) {
        PaymentReminder paymentReminder = paymentReminderService.getPaymentReminderByIdOrThrow(dto.id());
        paymentReminder.getAudit().validateIsActive("PaymentReminder");

        if (!Objects.equals(paymentReminder.getUser().getEmail(), dto.email()))
            throw new ConflictException("This reminder does not belong to the logged in user");

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
