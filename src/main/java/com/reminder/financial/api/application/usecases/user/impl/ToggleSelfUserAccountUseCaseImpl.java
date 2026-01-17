package com.reminder.financial.api.application.usecases.user.impl;

import com.reminder.financial.api.application.usecases.paymentreminder.ToggleReminderStatusUseCase;
import com.reminder.financial.api.application.usecases.user.ToggleSelfUserAccountUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.UserService;
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

        for (PaymentReminder reminder : paymentReminderService.listPaymentRemindersByEmail(userEmail)) {
            toggleReminderStatusUseCase.execute(reminder.getId(), enable, userEmail);
        }

        updateAuditStatus(user, enable, userEmail);
        userService.save(user);
    }

    private void updateAuditStatus(User user, boolean enable, String userEmail) {
        Instant now = Instant.now();

        user.getAudit().setEnabled(enable);
        user.getAudit().setToggledBy(userEmail);

        if (enable) {
            user.getAudit().setEnabledAt(now);
            user.getAudit().setDisabledAt(null);
        } else {
            user.getAudit().setEnabledAt(null);
            user.getAudit().setDisabledAt(now);
        }
    }
}
