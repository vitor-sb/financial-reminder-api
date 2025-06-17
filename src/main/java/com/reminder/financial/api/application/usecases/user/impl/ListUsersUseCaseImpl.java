package com.reminder.financial.api.application.usecases.user.impl;

import com.reminder.financial.api.application.dtos.user.ListUsersFilterQuery;
import com.reminder.financial.api.application.dtos.user.ListUsersSingleResult;
import com.reminder.financial.api.application.usecases.user.ListUsersUseCase;
import com.reminder.financial.api.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ListUsersUseCaseImpl implements ListUsersUseCase {

    @Autowired
    private UserService userService;

    @Override
    public Page<ListUsersSingleResult> execute(ListUsersFilterQuery query, Pageable pageable) {
        var usersPage = userService.listUsers(query, pageable);

        return usersPage.map(user -> new ListUsersSingleResult(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAudit().getEnabled(),
                user.getAudit().getDeleted()
        ));
    }
}
