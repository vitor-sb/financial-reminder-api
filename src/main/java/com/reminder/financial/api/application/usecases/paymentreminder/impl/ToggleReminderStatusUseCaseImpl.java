package com.reminder.financial.api.application.usecases.paymentreminder.impl;

import com.reminder.financial.api.application.exceptions.NotFoundException;
import com.reminder.financial.api.application.usecases.paymentreminder.ToggleReminderStatusUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
@Component
public class ToggleReminderStatusUseCaseImpl implements ToggleReminderStatusUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ToggleReminderStatusUseCaseImpl.class);
    private final PaymentReminderService paymentReminderService;

    private final PaymentTransactionService paymentTransactionService;

    @Override
    @Transactional
    public void execute(String reminderId, boolean enable, String userEmail) {
        PaymentReminder reminder = paymentReminderService.getPaymentReminderByIdOrThrow(reminderId);

        if (!reminder.getUser().getEmail().equals(userEmail)) {
            throw new NotFoundException("Payment Reminder not found.");
        }

        paymentTransactionService.togglePendingTransactionStatus(reminderId, enable, userEmail);

        reminder.getAudit().setEnabled(enable);
        if (enable) {
            reminder.getAudit().setEnabledAt(Instant.now());
            reminder.getAudit().setToggledBy(userEmail);
        }

        paymentReminderService.savePaymentReminder(reminder);
        logger.info("reminder toggled reminderId={} userId={} enabled={}", reminderId, reminder.getUser().getId(), enable);
    }
}
