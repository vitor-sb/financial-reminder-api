package com.reminder.financial.api.infrastructure.persistence;

import com.reminder.financial.api.domains.filters.UserFilter;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.repositories.UserSearch;
import com.reminder.financial.api.domains.repositories.jpa.UserRepository;
import com.reminder.financial.api.infrastructure.utils.AppConstants;
import com.reminder.financial.api.infrastructure.utils.SpecificationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;

@RequiredArgsConstructor
@Component
public class UserSpecificationSearch implements UserSearch {

    private final UserRepository userRepository;

    @Override
    public Page<User> searchByFilters(UserFilter query, Pageable pageable) {
        Instant fromInstant = null;
        if (query.createdFrom() != null) {
            fromInstant = query.createdFrom().atStartOfDay(AppConstants.DEFAULT_ZONE_ID).toInstant();
        }

        Instant untilInstant = null;
        if (query.createdUntil() != null) {
            untilInstant = query.createdUntil().plusDays(1).atStartOfDay(AppConstants.DEFAULT_ZONE_ID).toInstant();
        }

        Specification<User> spec = Specification
                .where(SpecificationFactory.<User>like("name", query.name()))
                .and(SpecificationFactory.like("email", query.email()))
                .and(SpecificationFactory.equal("role", query.role()))
                .and(SpecificationFactory.isGreaterThanOrEqualTo("audit.createdAt", fromInstant))
                .and(SpecificationFactory.isLessThan("audit.createdAt", untilInstant))
                .and(SpecificationFactory.equalInJoin("audit", "enabled", query.enabled()))
                .and(SpecificationFactory.equalInJoin("audit", "deleted", query.deleted()));

        return userRepository.findAll(spec, pageable);
    }
}
