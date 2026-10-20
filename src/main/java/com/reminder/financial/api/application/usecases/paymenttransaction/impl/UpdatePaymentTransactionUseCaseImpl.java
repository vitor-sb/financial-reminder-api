package com.reminder.financial.api.application.usecases.paymenttransaction.impl;

import com.reminder.financial.api.application.dtos.paymenttransaction.UpdatePaymentTransactionCommand;
import com.reminder.financial.api.application.exceptions.NotFoundException;
import com.reminder.financial.api.application.usecases.paymenttransaction.UpdatePaymentTransactionUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UpdatePaymentTransactionUseCaseImpl implements UpdatePaymentTransactionUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePaymentTransactionUseCaseImpl.class);

    private final PaymentTransactionService paymentTransactionService;

    @Override
    public void execute(String userEmail, String transactionId, UpdatePaymentTransactionCommand command) {
        PaymentTransaction transaction = paymentTransactionService.getTransactionById(transactionId)
                .orElseThrow(() -> new NotFoundException("Payment Transaction not found."));

        if (!transaction.getReminder().getUser().getEmail().equals(userEmail)) {
            throw new NotFoundException("Payment Transaction not found.");
        }

        transaction.getAudit().validateIsActive("PaymentTransaction");

        this.updateTransactionFields(command, transaction);

        paymentTransactionService.save(transaction);
        logger.info("transaction updated transactionId={} reminderId={}", transactionId, transaction.getReminder().getId());
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