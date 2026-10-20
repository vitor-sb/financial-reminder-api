package com.reminder.financial.api.infrastructure.persistence;

import com.reminder.financial.api.application.dtos.paymenttransaction.ListTransactionsFilterQuery;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.repositories.PaymentTransactionSearch;
import com.reminder.financial.api.domains.repositories.jpa.PaymentTransactionRepository;
import com.reminder.financial.api.infrastructure.utils.SpecificationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentTransactionSpecificationSearch implements PaymentTransactionSearch {

    private final PaymentTransactionRepository transactionRepository;

    @Override
    public Page<PaymentTransaction> searchByUserAndFilters(User user, ListTransactionsFilterQuery query, Pageable pageable) {
        var spec = Specification
                .where(SpecificationFactory.<PaymentTransaction>equalInJoin("reminder", "user", user))
                .and(SpecificationFactory.equal("id", query.transactionId()))
                .and(SpecificationFactory.equalInJoin("reminder", "id", query.reminderId()))
                .and(SpecificationFactory.isGreaterThanOrEqualTo("dueDate", query.dueDateFrom()))
                .and(SpecificationFactory.isLessThanOrEqualTo("dueDate", query.dueDateUntil()))
                .and(SpecificationFactory.equal("method", query.method()))
                .and(SpecificationFactory.equal("status", query.status()))
                .and(SpecificationFactory.equalInJoin("audit", "enabled", query.enabled()))
                .and(SpecificationFactory.equalInJoin("audit", "deleted", false));

        return transactionRepository.findAll(spec, pageable);
    }
}
