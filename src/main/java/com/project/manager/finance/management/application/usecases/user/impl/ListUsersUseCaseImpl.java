package com.project.manager.finance.management.application.usecases.user.impl;

import com.project.manager.finance.management.application.dtos.ListUsersFilterQuery;
import com.project.manager.finance.management.application.dtos.ListUsersSingleResult;
import com.project.manager.finance.management.application.usecases.user.ListUsersUseCase;
import com.project.manager.finance.management.domains.models.user.enums.RoleEnum;
import com.project.manager.finance.management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ListUsersUseCaseImpl implements ListUsersUseCase {
    @Autowired
    private UserService userService;

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
