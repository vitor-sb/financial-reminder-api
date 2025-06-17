package com.project.manager.finance.management.application.usecases.paymentreminder.impl;

import com.project.manager.finance.management.application.dtos.paymentreminder.UpdatePaymentReminderCommand;
import com.project.manager.finance.management.application.exceptions.NotFoundException;
import com.project.manager.finance.management.application.exceptions.UnauthorizedException;
import com.project.manager.finance.management.application.usecases.paymentreminder.UpdatePaymentReminderUseCase;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.PaymentTransactionService;
import com.project.manager.finance.management.infrastructure.utils.ObjectPropertyCopier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class UpdatePaymentReminderUseCaseImpl implements UpdatePaymentReminderUseCase {

    @Autowired
    PaymentReminderService paymentReminderService;

    @Autowired
    PaymentTransactionService paymentTransactionService;

    public PaymentReminder execute(UpdatePaymentReminderCommand dto) {
        PaymentReminder reminder = paymentReminderService.getPaymentReminderById(dto.reminderId())
                .orElseThrow(() -> new NotFoundException("Payment Reminder not found."));
        reminder.getAudit().validateIsActive("PaymentReminder");

        if (!reminder.getUser().getEmail().equals(dto.email())) {
            throw new UnauthorizedException("This reminder does not belong to the logged in user.");
        }

        ObjectPropertyCopier.copyNonNullProperties(dto, reminder);

        PaymentReminder updatedReminder = paymentReminderService.savePaymentReminder(reminder);

        if (Boolean.TRUE.equals(dto.updatePendingTransaction())) {
            paymentTransactionService
                    .findPendingTransactionByReminderId(reminder.getId())
                    .ifPresent(transaction -> {
                        ObjectPropertyCopier.copyNonNullProperties(reminder, transaction);
                        transaction.setPaymentValue(reminder.getBaseValue());
                        paymentTransactionService.save(transaction);
                    });
        }

        return updatedReminder;
    }
}
