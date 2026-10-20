package com.reminder.financial.api.domains.repositories;

import com.reminder.financial.api.domains.filters.TransactionFilter;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.models.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentTransactionSearch {

    Page<PaymentTransaction> searchByUserAndFilters(User user, TransactionFilter query, Pageable pageable);
}
