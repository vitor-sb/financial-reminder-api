package com.reminder.financial.api.application.usecases.paymentreminder.impl;

import com.reminder.financial.api.application.exceptions.UnauthorizedException;
import com.reminder.financial.api.application.usecases.paymentreminder.ToggleReminderStatusUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class ToggleReminderStatusUseCaseImpl implements ToggleReminderStatusUseCase {
    @Autowired
    private PaymentReminderService paymentReminderService;

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @Override
    @Transactional
    public void execute(String reminderId, boolean enable, String userEmail) {
        PaymentReminder reminder = paymentReminderService.getPaymentReminderByIdOrThrow(reminderId);

        if (!reminder.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("User does not own this reminder.");
        }

        paymentTransactionService.togglePendingTransactionStatus(reminderId, enable, userEmail);

        reminder.getAudit().setEnabled(enable);
        if (Boolean.TRUE.equals(enable)) {
            reminder.getAudit().setEnabledAt(Instant.now());
            reminder.getAudit().setEnabledBy(userEmail);
        }

        paymentReminderService.savePaymentReminder(reminder);
    }
}
