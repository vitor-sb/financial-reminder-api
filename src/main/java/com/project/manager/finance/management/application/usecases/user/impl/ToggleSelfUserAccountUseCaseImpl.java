package com.project.manager.finance.management.application.usecases.user.impl;

import com.project.manager.finance.management.application.usecases.paymentreminder.ToggleReminderStatusUseCase;
import com.project.manager.finance.management.application.usecases.user.ToggleSelfUserAccountUseCase;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ToggleSelfUserAccountUseCaseImpl implements ToggleSelfUserAccountUseCase {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentReminderService paymentReminderService;

    @Autowired
    private ToggleReminderStatusUseCase toggleReminderStatusUseCase;


    @Override
    @Transactional
    public void execute(String userEmail, boolean enable) {
        userService.toggleUserAccountStatus(userEmail, enable);

        paymentReminderService.listPaymentRemindersByEmail(userEmail)
                .forEach(reminder -> {
                    toggleReminderStatusUseCase.execute(reminder.getId(), enable, userEmail);
                });
    }
}
