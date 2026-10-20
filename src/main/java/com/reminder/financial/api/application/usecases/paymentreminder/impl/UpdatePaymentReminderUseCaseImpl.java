package com.reminder.financial.api.application.usecases.paymentreminder.impl;

import com.reminder.financial.api.application.dtos.paymentreminder.UpdatePaymentReminderCommand;
import com.reminder.financial.api.domains.exceptions.NotFoundException;
import com.reminder.financial.api.application.usecases.paymentreminder.UpdatePaymentReminderUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@RequiredArgsConstructor
@Component
public class UpdatePaymentReminderUseCaseImpl implements UpdatePaymentReminderUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePaymentReminderUseCaseImpl.class);

    private final PaymentReminderService paymentReminderService;

    private final PaymentTransactionService paymentTransactionService;

    public PaymentReminder execute(String userEmail, String reminderId, UpdatePaymentReminderCommand command) {
        PaymentReminder reminder = paymentReminderService.getPaymentReminderByIdOrThrow(reminderId);

        if (!reminder.getUser().getEmail().equals(userEmail)) {
            throw new NotFoundException("Payment Reminder not found.");
        }

        reminder.getAudit().validateIsActive("PaymentReminder");

        this.updateReminderFields(command, reminder);

        PaymentReminder updatedReminder = paymentReminderService.savePaymentReminder(reminder);

        paymentTransactionService
                .findPendingTransactionByReminderId(reminder.getId())
                .ifPresent(transaction -> this.updatePendingTransactionDetails(command, transaction));

        logger.info("reminder updated reminderId={} userId={}", reminderId, reminder.getUser().getId());

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
