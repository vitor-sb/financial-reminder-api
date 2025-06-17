package com.reminder.financial.api.application.usecases.paymenttransaction.impl;

import com.reminder.financial.api.application.dtos.paymenttransaction.ListTransactionsResult;
import com.reminder.financial.api.application.exceptions.UnauthorizedException;
import com.reminder.financial.api.application.usecases.paymenttransaction.ListTransactionsByReminderUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ListTransactionsByReminderUseCaseImpl implements ListTransactionsByReminderUseCase {

    @Autowired
    private PaymentTransactionService transactionService;

    @Autowired
    private PaymentReminderService paymentReminderService;

    @Override
    public Page<ListTransactionsResult> execute(String loggedUserEmail, String reminderId, Pageable pageable) {
        PaymentReminder reminder = paymentReminderService.getPaymentReminderByIdOrThrow(reminderId);
        reminder.getAudit().validateIsActive("PaymentReminder");

        if (!reminder.getUser().getEmail().equals(loggedUserEmail))
            throw new UnauthorizedException("You do not have permission to view transactions for this reminder.");

        var transactionPage = transactionService.listTransactionsByReminder(reminderId, pageable);

        return transactionPage.map(transaction -> new ListTransactionsResult(
                transaction.getId(),
                transaction.getPaymentDate(),
                transaction.getInstallmentNumber(),
                transaction.getPaymentValue(),
                transaction.getMethod(),
                transaction.getStatus()
        ));
    }
}