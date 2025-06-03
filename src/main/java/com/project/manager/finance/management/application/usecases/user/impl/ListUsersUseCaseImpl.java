package com.project.manager.finance.management.application.usecases.user.impl;

import com.project.manager.finance.management.application.dtos.ListUsersSingleResult;
import com.project.manager.finance.management.application.usecases.user.ListUsersUseCase;
import com.project.manager.finance.management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListUsersUseCaseImpl implements ListUsersUseCase {
    @Autowired
    private UserService userService;

    public List<ListUsersSingleResult> execute() {
        var users = userService.listUsers();

        return users.stream()
                .map(user -> new ListUsersSingleResult(
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getAudit().getEnabled(),
                        user.getAudit().getDeleted()
                ))
                .toList();
    }
}
