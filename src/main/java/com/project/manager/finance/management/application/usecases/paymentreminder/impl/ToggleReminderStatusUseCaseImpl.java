package com.project.manager.finance.management.application.usecases.paymentreminder.impl;

import com.project.manager.finance.management.application.exceptions.UnauthorizedException;
import com.project.manager.finance.management.application.usecases.paymentreminder.ToggleReminderStatusUseCase;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.PaymentTransactionService;
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
