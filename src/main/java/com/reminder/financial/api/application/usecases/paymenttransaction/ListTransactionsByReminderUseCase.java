package com.reminder.financial.api.application.usecases.paymenttransaction;

import com.reminder.financial.api.application.dtos.paymenttransaction.ListTransactionsResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListTransactionsByReminderUseCase {
    Page<ListTransactionsResult> execute(String loggedUserEmail, String reminderId, Pageable pageable);
}
