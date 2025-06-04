package com.project.manager.finance.management.application.usecases.paymentreminder.impl;

import com.project.manager.finance.management.application.dtos.GetPaymentReminderQuery;
import com.project.manager.finance.management.application.dtos.PaymentReminderResult;
import com.project.manager.finance.management.application.usecases.paymentreminder.GetPaymentReminderUseCase;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.application.exceptions.ConflictException;
import com.project.manager.finance.management.application.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GetPaymentReminderUseCaseImpl implements GetPaymentReminderUseCase {
    @Autowired
    PaymentReminderService paymentReminderService;

    public PaymentReminderResult execute(GetPaymentReminderQuery dto) {
        PaymentReminder paymentReminder = paymentReminderService.getPaymentReminderById(dto.id()).orElseThrow(
                () -> new NotFoundException("Payment reminder not found")
        );

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
