package com.project.manager.finance.management.application.usecases.paymenttransaction.impl;

import com.project.manager.finance.management.application.dtos.paymenttransaction.PayTransactionCommand;
import com.project.manager.finance.management.application.exceptions.ConflictException;
import com.project.manager.finance.management.application.exceptions.NotFoundException;
import com.project.manager.finance.management.application.exceptions.UnauthorizedException;
import com.project.manager.finance.management.application.usecases.paymenttransaction.PayTransactionUseCase;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.models.reminder.PaymentTransaction;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionStatusEnum;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.PaymentTransactionService;
import com.project.manager.finance.management.infrastructure.utils.ObjectPropertyCopier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PayTransactionUseCaseImpl implements PayTransactionUseCase {

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @Autowired
    private PaymentReminderService paymentReminderService;

    @Override
    public void execute(PayTransactionCommand command) {
        PaymentTransaction transaction = paymentTransactionService.getTransactionById(command.transactionId())
                .orElseThrow(() -> new NotFoundException("PaymentTransaction not found"));

        transaction.getAudit().validateIsActive("PaymentTransaction");

        if (transaction.getStatus() != TransactionStatusEnum.PENDENTE)
            throw new ConflictException("Only transactions with 'PENDENTE' status can be paid. Transaction status: " + transaction.getStatus());

        PaymentReminder reminder = paymentReminderService.getPaymentReminderById(transaction.getReminder().getId())
                .orElseThrow(() -> new NotFoundException("Associated PaymentReminder not found"));

        reminder.getAudit().validateIsActive("PaymentReminder");

        if (!reminder.getUser().getEmail().equals(command.loggedUserEmail()))
            throw new UnauthorizedException("You do not have permission to update this transaction.");

        ObjectPropertyCopier.copyNonNullProperties(command.transactionDetails(), transaction);

        paymentTransactionService.processSuccessfulPayment(transaction);
    }
}