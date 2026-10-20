package com.reminder.financial.api.infrastructure.persistence;

import com.reminder.financial.api.application.dtos.paymentreminder.ListPaymentRemindersFilterQuery;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.repositories.PaymentReminderSearch;
import com.reminder.financial.api.domains.repositories.jpa.PaymentReminderRepository;
import com.reminder.financial.api.infrastructure.utils.SpecificationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentReminderSpecificationSearch implements PaymentReminderSearch {

    private final PaymentReminderRepository paymentReminderRepository;

    @Override
    public Page<PaymentReminder> searchByUserAndFilters(
            User user,
            ListPaymentRemindersFilterQuery query,
            Pageable pageable
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

        return paymentReminderRepository.findAll(spec, pageable);
    }
}
