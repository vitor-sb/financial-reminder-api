package com.project.manager.finance.management.application.usecases.paymenttransaction;

import com.project.manager.finance.management.application.dtos.paymenttransaction.ListTransactionsResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListTransactionsByReminderUseCase {
    Page<ListTransactionsResult> execute(String loggedUserEmail, String reminderId, Pageable pageable);
}
