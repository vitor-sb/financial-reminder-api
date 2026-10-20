package com.reminder.financial.api.domains.repositories;

import com.reminder.financial.api.domains.filters.TransactionFilter;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.pagination.PageResult;
import com.reminder.financial.api.domains.pagination.PageRequest;

public interface PaymentTransactionSearch {

    PageResult<PaymentTransaction> searchByUserAndFilters(User user, TransactionFilter query, PageRequest pageable);
}
