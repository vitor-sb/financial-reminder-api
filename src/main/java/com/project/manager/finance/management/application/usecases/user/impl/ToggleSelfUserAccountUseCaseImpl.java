package com.project.manager.finance.management.application.usecases.user.impl;

import com.project.manager.finance.management.application.exceptions.NotFoundException;
import com.project.manager.finance.management.application.usecases.paymentreminder.ToggleReminderStatusUseCase;
import com.project.manager.finance.management.application.usecases.user.ToggleSelfUserAccountUseCase;
import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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
        User user = userService.findUserByEmailOrThrow(userEmail);
        user.getAudit().validateIsDeleted("User Account");

        if (user.getAudit().getEnabled().equals(enable)) {
            return;
        }

        paymentReminderService.listPaymentRemindersByEmail(userEmail)
                .forEach(reminder -> {
                    toggleReminderStatusUseCase.execute(reminder.getId(), enable, userEmail);
                });

        user.getAudit().setDeleted(true);
        user.getAudit().setDeletedAt(Instant.now());
        user.getAudit().setDeletedBy(userEmail);
        userService.save(user);
    }
}
