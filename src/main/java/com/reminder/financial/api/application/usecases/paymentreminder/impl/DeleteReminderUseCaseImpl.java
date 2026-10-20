package com.reminder.financial.api.application.usecases.paymentreminder.impl;

import com.reminder.financial.api.application.exceptions.NotFoundException;
import com.reminder.financial.api.application.usecases.paymentreminder.DeletePaymentReminderUseCase;
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
public class DeleteReminderUseCaseImpl implements DeletePaymentReminderUseCase {

    private static final Logger logger = LoggerFactory.getLogger(DeleteReminderUseCaseImpl.class);
    private final PaymentReminderService paymentReminderService;

    private final PaymentTransactionService paymentTransactionService;

    @Override
    @Transactional
    public void execute(String reminderId, String userEmail) {
        PaymentReminder reminder = paymentReminderService.getPaymentReminderByIdOrThrow(reminderId);

        if (!reminder.getUser().getEmail().equals(userEmail)) {
            throw new NotFoundException("Payment Reminder not found.");
        }

        paymentTransactionService.cancelAndSoftDeletePendingTransaction(reminderId, userEmail);

        reminder.getAudit().setDeleted(true);
        reminder.getAudit().setDeletedBy(userEmail);
        reminder.getAudit().setDeletedAt(Instant.now());
        paymentReminderService.savePaymentReminder(reminder);
        logger.info("reminder deleted reminderId={} userId={}", reminderId, reminder.getUser().getId());
    }
}
