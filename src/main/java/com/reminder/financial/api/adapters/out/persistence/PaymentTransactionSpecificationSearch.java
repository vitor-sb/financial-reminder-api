package com.reminder.financial.api.adapters.out.persistence;

import com.reminder.financial.api.domains.filters.TransactionFilter;
import com.reminder.financial.api.domains.models.reminder.PaymentTransaction;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.repositories.PaymentTransactionSearch;
import com.reminder.financial.api.domains.repositories.jpa.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import com.reminder.financial.api.domains.pagination.PageResult;
import com.reminder.financial.api.domains.pagination.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentTransactionSpecificationSearch implements PaymentTransactionSearch {

    private final PaymentTransactionRepository transactionRepository;

    @Override
    public PageResult<PaymentTransaction> searchByUserAndFilters(User user, TransactionFilter query, PageRequest pageable) {
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

        return PageConversion.toResult(transactionRepository.findAll(spec, PageConversion.toPageable(pageable)), pageable);
    }
}
