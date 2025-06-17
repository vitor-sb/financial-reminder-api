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
    public void execute(UpdatePaymentTransactionCommand dto) {
        PaymentTransaction transaction = paymentTransactionService.getTransactionById(dto.transactionId())
                .orElseThrow(() -> new NotFoundException("Payment Transaction not found."));

        if (!transaction.getReminder().getUser().getEmail().equals(dto.loggedUserEmail())) {
            throw new UnauthorizedException("You do not have permission to update this transaction.");
        }

        transaction.getAudit().validateIsActive("PaymentTransaction");

        this.updateTransactionFields(dto, transaction);

        paymentTransactionService.save(transaction);
    }

    private void updateTransactionFields(UpdatePaymentTransactionCommand dto, PaymentTransaction transaction) {
        if (dto.paymentValue() != null) {
            transaction.setPaymentValue(dto.paymentValue());
        }
        if (dto.method() != null) {
            transaction.setMethod(dto.method());
        }
    }
}