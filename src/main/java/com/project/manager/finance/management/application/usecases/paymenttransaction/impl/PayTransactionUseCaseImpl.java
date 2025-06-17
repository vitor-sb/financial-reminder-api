package com.project.manager.finance.management.application.usecases.paymenttransaction.impl;

import com.project.manager.finance.management.application.dtos.paymenttransaction.PayTransactionCommand;
import com.project.manager.finance.management.application.dtos.paymenttransaction.PaymentExecutionDetails;
import com.project.manager.finance.management.application.exceptions.ConflictException;
import com.project.manager.finance.management.application.exceptions.NotFoundException;
import com.project.manager.finance.management.application.exceptions.UnauthorizedException;
import com.project.manager.finance.management.application.usecases.paymenttransaction.PayTransactionUseCase;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.models.reminder.PaymentTransaction;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionStatusEnum;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PayTransactionUseCaseImpl implements PayTransactionUseCase {

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @Autowired
    private PaymentReminderService paymentReminderService;

    @Override
    @Transactional
    public void execute(PayTransactionCommand command) {
        PaymentTransaction transaction = paymentTransactionService.getTransactionById(command.transactionId())
                .orElseThrow(() -> new NotFoundException("PaymentTransaction not found"));

        transaction.getAudit().validateIsActive("PaymentTransaction");

        if (transaction.getStatus() != TransactionStatusEnum.PENDENTE && transaction.getStatus() != TransactionStatusEnum.VENCIDO)
            throw new ConflictException("Only transactions with 'PENDENTE' or 'VENCIDO' status can be paid. Transaction status: " + transaction.getStatus());

        PaymentReminder reminder = paymentReminderService.getPaymentReminderByIdOrThrow(transaction.getReminder().getId());
        reminder.getAudit().validateIsActive("PaymentReminder");

        if (!reminder.getUser().getEmail().equals(command.loggedUserEmail()))
            throw new UnauthorizedException("You do not have permission to update this transaction.");

        this.updateTransactionFromPaymentDetails(command.transactionDetails(), transaction);

        paymentTransactionService.processSuccessfulPayment(transaction);
    }

    private void updateTransactionFromPaymentDetails(PaymentExecutionDetails details, PaymentTransaction transaction) {
        if (details.method() != null) {
            transaction.setMethod(details.method());
        }
        if (details.paymentValue() != null) {
            transaction.setPaymentValue(details.paymentValue());
        }
    }
}