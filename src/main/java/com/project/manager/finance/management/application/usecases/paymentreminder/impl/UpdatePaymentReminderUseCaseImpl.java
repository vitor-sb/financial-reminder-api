package com.project.manager.finance.management.application.usecases.paymentreminder.impl;

import com.project.manager.finance.management.application.dtos.paymentreminder.UpdatePaymentReminderCommand;
import com.project.manager.finance.management.application.exceptions.NotFoundException;
import com.project.manager.finance.management.application.exceptions.UnauthorizedException;
import com.project.manager.finance.management.application.usecases.paymentreminder.UpdatePaymentReminderUseCase;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.models.reminder.PaymentTransaction;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Component
public class UpdatePaymentReminderUseCaseImpl implements UpdatePaymentReminderUseCase {

    @Autowired
    PaymentReminderService paymentReminderService;

    @Autowired
    PaymentTransactionService paymentTransactionService;

    public PaymentReminder execute(UpdatePaymentReminderCommand command) {
        PaymentReminder reminder = paymentReminderService.getPaymentReminderByIdOrThrow(command.reminderId());
        reminder.getAudit().validateIsActive("PaymentReminder");

        if (!reminder.getUser().getEmail().equals(command.email())) {
            throw new UnauthorizedException("This reminder does not belong to the logged in user.");
        }

        this.updateReminderFields(command, reminder);

        PaymentReminder updatedReminder = paymentReminderService.savePaymentReminder(reminder);

        if (Boolean.TRUE.equals(command.updatePendingTransaction())) {
            paymentTransactionService
                    .findPendingTransactionByReminderId(reminder.getId())
                    .ifPresent(transaction -> this.updatePendingTransactionDetails(command, transaction));
        }

        return updatedReminder;
    }

    private void updateReminderFields(UpdatePaymentReminderCommand command, PaymentReminder reminder) {
        if (StringUtils.hasText(command.title())) {
            reminder.setTitle(command.title());
        }
        if (StringUtils.hasText(command.description())) {
            reminder.setDescription(command.description());
        }
        if (command.method() != null) {
            reminder.setMethod(command.method());
        }
        if (command.value() != null) {
            reminder.setBaseValue(command.value());
        }
    }

    private void updatePendingTransactionDetails(UpdatePaymentReminderCommand command, PaymentTransaction transaction) {
        if (command.method() != null) {
            transaction.setMethod(command.method());
        }

        if (command.value() != null) {
            transaction.setPaymentValue(command.value());
        }

        paymentTransactionService.save(transaction);
    }
}
