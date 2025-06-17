package com.project.manager.finance.management.application.usecases.paymentreminder.impl;

import com.project.manager.finance.management.application.exceptions.NotFoundException;
import com.project.manager.finance.management.application.exceptions.UnauthorizedException;
import com.project.manager.finance.management.application.usecases.paymentreminder.DeletePaymentReminderUseCase;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class DeleteReminderUseCaseImpl implements DeletePaymentReminderUseCase {
    @Autowired
    private PaymentReminderService paymentReminderService;

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @Override
    @Transactional
    public void execute(String reminderId, String userEmail) {
        PaymentReminder reminder = paymentReminderService.getPaymentReminderByIdOrThrow(reminderId);

        if (!reminder.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("User does not own this reminder.");
        }

        paymentTransactionService.cancelAndSoftDeletePendingTransaction(reminderId, userEmail);

        reminder.getAudit().setDeleted(true);
        reminder.getAudit().setDeletedBy(userEmail);
        reminder.getAudit().setDeletedAt(Instant.now());
        paymentReminderService.savePaymentReminder(reminder);
    }
}
