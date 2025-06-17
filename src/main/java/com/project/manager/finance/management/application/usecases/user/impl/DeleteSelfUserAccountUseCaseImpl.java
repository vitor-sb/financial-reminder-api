package com.project.manager.finance.management.application.usecases.user.impl;

import com.project.manager.finance.management.application.usecases.user.DeleteSelfUserAccountUseCase;
import com.project.manager.finance.management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteSelfUserAccountUseCaseImpl implements DeleteSelfUserAccountUseCase {
    @Autowired
    private UserService userService;

    @Override
    public void execute(String userEmail) {
        userService.softDeleteUserAccount(userEmail);
    }
}