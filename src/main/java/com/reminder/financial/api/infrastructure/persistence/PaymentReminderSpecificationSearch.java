package com.reminder.financial.api.infrastructure.persistence;

import com.reminder.financial.api.domains.filters.ReminderFilter;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.repositories.PaymentReminderSearch;
import com.reminder.financial.api.domains.repositories.jpa.PaymentReminderRepository;
import com.reminder.financial.api.infrastructure.utils.SpecificationFactory;
import lombok.RequiredArgsConstructor;
import com.reminder.financial.api.domains.pagination.PageResult;
import com.reminder.financial.api.domains.pagination.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentReminderSpecificationSearch implements PaymentReminderSearch {

    private final PaymentReminderRepository paymentReminderRepository;

    @Override
    public PageResult<PaymentReminder> searchByUserAndFilters(
            User user,
            ReminderFilter query,
            PageRequest pageable
    ) {
        Specification<PaymentReminder> spec = Specification
                .where(SpecificationFactory.<PaymentReminder>equal("user", user))
                .and(SpecificationFactory.equal("id", query.reminderId()))
                .and(SpecificationFactory.isGreaterThanOrEqualTo("dueDate", query.dueDateFrom()))
                .and(SpecificationFactory.isLessThanOrEqualTo("dueDate", query.dueDateUntil()))
                .and(SpecificationFactory.equal("recurrence", query.recurrence()))
                .and(SpecificationFactory.equal("method", query.method()))
                .and(SpecificationFactory.equalInJoin("audit", "enabled", query.enabled()))
                .and(SpecificationFactory.equalInJoin("audit", "deleted", false));

        return PageConversion.toResult(paymentReminderRepository.findAll(spec, PageConversion.toPageable(pageable)), pageable);
    }
}
