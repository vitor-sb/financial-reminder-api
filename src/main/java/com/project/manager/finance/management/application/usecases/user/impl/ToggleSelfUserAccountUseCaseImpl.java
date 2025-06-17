package com.project.manager.finance.management.application.usecases.user.impl;

import com.project.manager.finance.management.application.usecases.user.ToggleSelfUserAccountUseCase;
import com.project.manager.finance.management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ToggleSelfUserAccountUseCaseImpl implements ToggleSelfUserAccountUseCase {

    @Autowired
    private UserService userService;

    @Override
    public void execute(String userEmail, boolean enable) {
        userService.toggleUserAccountStatus(userEmail, enable);
    }
}
