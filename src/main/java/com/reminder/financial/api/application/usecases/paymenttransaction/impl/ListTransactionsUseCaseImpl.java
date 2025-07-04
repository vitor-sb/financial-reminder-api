package com.reminder.financial.api.application.usecases.paymenttransaction.impl;

import com.reminder.financial.api.application.dtos.paymenttransaction.ListTransactionsFilterQuery;
import com.reminder.financial.api.application.dtos.paymenttransaction.ListTransactionsResult;
import com.reminder.financial.api.application.usecases.paymenttransaction.ListTransactionsUseCase;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import com.reminder.financial.api.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ListTransactionsUseCaseImpl implements ListTransactionsUseCase {

    @Autowired
    private PaymentTransactionService transactionService;

    @Autowired
    private UserService userService;

    @Override
    public Page<ListTransactionsResult> execute(String loggedUserEmail, ListTransactionsFilterQuery query, Pageable pageable) {
        User user = userService.findValidUserByEmail(loggedUserEmail);

        var transactionPage = transactionService.listTransactionsByUserAndFilters(user, query, pageable);

        return transactionPage.map(transaction -> new ListTransactionsResult(
                transaction.getId(),
                transaction.getReminder().getId(),
                transaction.getDueDate(),
                transaction.getPaymentDate(),
                transaction.getInstallmentNumber(),
                transaction.getPaymentValue(),
                transaction.getMethod(),
                transaction.getStatus(),
                transaction.getAudit().getEnabled(),
                transaction.getAudit().getDeleted()
        ));
    }
}