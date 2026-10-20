package com.reminder.financial.api.application.usecases.paymenttransaction;

import com.reminder.financial.api.domains.filters.TransactionFilter;
import com.reminder.financial.api.application.dtos.paymenttransaction.ListTransactionsResult;
import com.reminder.financial.api.domains.pagination.PageResult;
import com.reminder.financial.api.domains.pagination.PageRequest;

public interface ListTransactionsUseCase {
    PageResult<ListTransactionsResult> execute(String loggedUserEmail, TransactionFilter query, PageRequest pageable);
}
