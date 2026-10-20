package com.reminder.financial.api.application.usecases.paymenttransaction.impl;

import com.reminder.financial.api.application.dtos.paymenttransaction.PayTransactionDetailsCommand;
import com.reminder.financial.api.application.exceptions.ConflictException;
import com.reminder.financial.api.application.exceptions.NotFoundException;
import com.reminder.financial.api.application.usecases.paymenttransaction.PayTransactionUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.models.reminder.enums.TransactionStatusEnum;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@RequiredArgsConstructor
@Component
public class PayTransactionUseCaseImpl implements PayTransactionUseCase {

    private static final Logger logger = LoggerFactory.getLogger(PayTransactionUseCaseImpl.class);

    private final PaymentTransactionService paymentTransactionService;

    private final PaymentReminderService paymentReminderService;

    private static final Set<TransactionStatusEnum> VALID_PAY_STATUSES = Set.of(
            TransactionStatusEnum.PENDENTE,
            TransactionStatusEnum.VENCIDO
    );

    @Override
    @Transactional
    public void execute(String userEmail, String transactionId, PayTransactionDetailsCommand command) {
        PaymentTransaction transaction = paymentTransactionService.getTransactionById(transactionId)
                .orElseThrow(() -> new NotFoundException("Payment Transaction not found."));

        PaymentReminder reminder = paymentReminderService.getPaymentReminderByIdOrThrow(transaction.getReminder().getId());
        if (!reminder.getUser().getEmail().equals(userEmail))
            throw new NotFoundException("Payment Transaction not found.");

        transaction.getAudit().validateIsActive("PaymentTransaction");

        if (!VALID_PAY_STATUSES.contains(transaction.getStatus()))
            throw new ConflictException("This transaction cannot be paid. Transaction status: " + transaction.getStatus());

        reminder.getAudit().validateIsActive("PaymentReminder");

        this.updateTransactionFromPaymentDetails(command, transaction);

        paymentTransactionService.processSuccessfulPayment(transaction);
        logger.info("transaction paid transactionId={} reminderId={} userId={}", transactionId, reminder.getId(), reminder.getUser().getId());
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