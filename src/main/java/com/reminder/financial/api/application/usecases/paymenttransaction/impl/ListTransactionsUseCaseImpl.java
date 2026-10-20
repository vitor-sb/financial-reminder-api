package com.reminder.financial.api.application.usecases.paymenttransaction.impl;

import com.reminder.financial.api.domains.filters.TransactionFilter;
import com.reminder.financial.api.application.dtos.paymenttransaction.ListTransactionsResult;
import com.reminder.financial.api.application.usecases.paymenttransaction.ListTransactionsUseCase;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.PaymentTransactionService;
import com.reminder.financial.api.domains.services.UserService;
import lombok.RequiredArgsConstructor;
import com.reminder.financial.api.domains.pagination.PageResult;
import com.reminder.financial.api.domains.pagination.PageRequest;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ListTransactionsUseCaseImpl implements ListTransactionsUseCase {

    private final PaymentTransactionService transactionService;

    private final UserService userService;

    @Override
    public PageResult<ListTransactionsResult> execute(String loggedUserEmail, TransactionFilter query, PageRequest pageable) {
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