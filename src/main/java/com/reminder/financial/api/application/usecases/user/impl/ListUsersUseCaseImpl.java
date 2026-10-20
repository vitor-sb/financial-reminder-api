package com.reminder.financial.api.application.usecases.user.impl;

import com.reminder.financial.api.domains.filters.UserFilter;
import com.reminder.financial.api.application.dtos.user.ListUsersSingleResult;
import com.reminder.financial.api.application.usecases.user.ListUsersUseCase;
import com.reminder.financial.api.domains.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ListUsersUseCaseImpl implements ListUsersUseCase {

    private final UserService userService;

    @Override
    public Page<ListUsersSingleResult> execute(UserFilter query, Pageable pageable) {
        var usersPage = userService.listUsers(query, pageable);

        return usersPage.map(user -> new ListUsersSingleResult(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAudit().getCreatedAt(),
                user.getAudit().getEnabled(),
                user.getAudit().getDeleted()
        ));
    }
}
