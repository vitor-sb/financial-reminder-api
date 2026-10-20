package com.reminder.financial.api.application.usecases.user.impl;

import com.reminder.financial.api.application.usecases.paymentreminder.DeletePaymentReminderUseCase;
import com.reminder.financial.api.application.usecases.user.DeleteSelfUserAccountUseCase;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class DeleteSelfUserAccountUseCaseImpl implements DeleteSelfUserAccountUseCase {

    private static final Logger logger = LoggerFactory.getLogger(DeleteSelfUserAccountUseCaseImpl.class);
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
                .forEach(reminder -> deleteReminderUseCase.execute(reminder.getId(), userEmail));

        user.getAudit().setDeleted(true);
        user.getAudit().setDeletedAt(Instant.now());
        user.getAudit().setDeletedBy(userEmail);
        userService.save(user);
        logger.info("user deleted userId={}", user.getId());
    }
}