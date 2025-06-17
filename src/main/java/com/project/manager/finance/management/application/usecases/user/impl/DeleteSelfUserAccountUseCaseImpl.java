package com.project.manager.finance.management.application.usecases.user.impl;

import com.project.manager.finance.management.application.usecases.paymentreminder.DeletePaymentReminderUseCase;
import com.project.manager.finance.management.application.usecases.user.DeleteSelfUserAccountUseCase;
import com.project.manager.finance.management.domains.models.user.User;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class DeleteSelfUserAccountUseCaseImpl implements DeleteSelfUserAccountUseCase {
    @Autowired
    private UserService userService;

    @Autowired
    private PaymentReminderService paymentReminderService;

    @Autowired
    private DeletePaymentReminderUseCase deleteReminderUseCase;

    @Override
    @Transactional
    public void execute(String userEmail) {
        User user = userService.findUserByEmailOrThrow(userEmail);

        if (Boolean.TRUE.equals(user.getAudit().getDeleted())) {
            return;
        }

        paymentReminderService.listPaymentRemindersByEmail(userEmail)
                .forEach(reminder -> {
                    deleteReminderUseCase.execute(reminder.getId(), userEmail);
                });

        user.getAudit().setDeleted(true);
        user.getAudit().setDeletedAt(Instant.now());
        user.getAudit().setDeletedBy(userEmail);
        userService.save(user);
    }
}