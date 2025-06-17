package com.project.manager.finance.management.application.usecases.paymenttransaction.impl;

import com.project.manager.finance.management.application.dtos.paymenttransaction.PayTransactionCommand;
import com.project.manager.finance.management.application.dtos.paymenttransaction.UpdatePaymentTransactionCommand;
import com.project.manager.finance.management.application.exceptions.ConflictException;
import com.project.manager.finance.management.application.exceptions.NotFoundException;
import com.project.manager.finance.management.application.exceptions.UnauthorizedException;
import com.project.manager.finance.management.application.usecases.paymenttransaction.PayTransactionUseCase;
import com.project.manager.finance.management.application.usecases.paymenttransaction.UpdatePaymentTransactionUseCase;
import com.project.manager.finance.management.domains.models.reminder.PaymentReminder;
import com.project.manager.finance.management.domains.models.reminder.PaymentTransaction;
import com.project.manager.finance.management.domains.models.reminder.enums.TransactionStatusEnum;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.PaymentTransactionService;
import com.project.manager.finance.management.infrastructure.utils.ObjectPropertyCopier;
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

        ObjectPropertyCopier.copyNonNullProperties(dto, transaction);

        paymentTransactionService.save(transaction);
    }
}