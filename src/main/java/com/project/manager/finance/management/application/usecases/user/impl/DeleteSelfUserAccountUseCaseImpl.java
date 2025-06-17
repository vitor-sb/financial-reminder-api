package com.project.manager.finance.management.application.usecases.user.impl;

import com.project.manager.finance.management.application.usecases.paymentreminder.DeleteReminderUseCase;
import com.project.manager.finance.management.application.usecases.user.DeleteSelfUserAccountUseCase;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeleteSelfUserAccountUseCaseImpl implements DeleteSelfUserAccountUseCase {
    @Autowired
    private UserService userService;

    @Autowired
    private PaymentReminderService paymentReminderService;

    @Autowired
    private DeleteReminderUseCase deleteReminderUseCase;

    @Override
    @Transactional
    public void execute(String userEmail) {
        paymentReminderService.listPaymentRemindersByEmail(userEmail)
                .forEach(reminder -> {
                    deleteReminderUseCase.execute(reminder.getId(), userEmail);
                });

        userService.softDeleteUserAccount(userEmail);
    }
}