package com.reminder.financial.api.application.usecases.paymenttransaction.impl;

import com.reminder.financial.api.application.dtos.paymenttransaction.UpdatePaymentTransactionCommand;
import com.reminder.financial.api.application.exceptions.NotFoundException;
import com.reminder.financial.api.application.exceptions.UnauthorizedException;
import com.reminder.financial.api.application.usecases.paymenttransaction.UpdatePaymentTransactionUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdatePaymentTransactionUseCaseImpl implements UpdatePaymentTransactionUseCase {

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @Override
    public void execute(String userEmail, String transactionId, UpdatePaymentTransactionCommand command) {
        PaymentTransaction transaction = paymentTransactionService.getTransactionById(transactionId)
                .orElseThrow(() -> new NotFoundException("Payment Transaction not found."));

        if (!transaction.getReminder().getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("You do not have permission to update this transaction.");
        }

        transaction.getAudit().validateIsActive("PaymentTransaction");

        this.updateTransactionFields(command, transaction);

        paymentTransactionService.save(transaction);
    }

    private void updateTransactionFields(UpdatePaymentTransactionCommand command, PaymentTransaction transaction) {
        if (command.paymentValue() != null) {
            transaction.setPaymentValue(command.paymentValue());
        }
        if (command.method() != null) {
            transaction.setMethod(command.method());
        }
    }
}