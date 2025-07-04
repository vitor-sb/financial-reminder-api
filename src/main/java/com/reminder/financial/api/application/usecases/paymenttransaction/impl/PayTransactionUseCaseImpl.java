package com.reminder.financial.api.application.usecases.paymenttransaction.impl;

import com.reminder.financial.api.application.dtos.paymenttransaction.PayTransactionDetailsCommand;
import com.reminder.financial.api.application.exceptions.ConflictException;
import com.reminder.financial.api.application.exceptions.NotFoundException;
import com.reminder.financial.api.application.exceptions.UnauthorizedException;
import com.reminder.financial.api.application.usecases.paymenttransaction.PayTransactionUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class PayTransactionUseCaseImpl implements PayTransactionUseCase {

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @Autowired
    private PaymentReminderService paymentReminderService;

    private static final Set<TransactionStatusEnum> VALID_PAY_STATUSES = Set.of(
            TransactionStatusEnum.PENDENTE,
            TransactionStatusEnum.VENCIDO
    );

    @Override
    @Transactional
    public void execute(String userEmail, String transactionId, PayTransactionDetailsCommand command) {
        PaymentTransaction transaction = paymentTransactionService.getTransactionById(transactionId)
                .orElseThrow(() -> new NotFoundException("PaymentTransaction not found"));

        transaction.getAudit().validateIsActive("PaymentTransaction");

        if (!VALID_PAY_STATUSES.contains(transaction.getStatus()))
            throw new ConflictException("This transaction cannot be paid. Transaction status: " + transaction.getStatus());

        PaymentReminder reminder = paymentReminderService.getPaymentReminderByIdOrThrow(transaction.getReminder().getId());
        reminder.getAudit().validateIsActive("PaymentReminder");

        if (!reminder.getUser().getEmail().equals(userEmail))
            throw new UnauthorizedException("You do not have permission to update this transaction.");

        this.updateTransactionFromPaymentDetails(command, transaction);

        paymentTransactionService.processSuccessfulPayment(transaction);
    }

    private void updateTransactionFromPaymentDetails(PayTransactionDetailsCommand details, PaymentTransaction transaction) {
        if (details.method() != null) {
            transaction.setMethod(details.method());
        }
        if (details.paymentValue() != null) {
            transaction.setPaymentValue(details.paymentValue());
        }
    }
}