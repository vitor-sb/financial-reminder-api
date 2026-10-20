package com.reminder.financial.api.application.usecases.user.impl;

import com.reminder.financial.api.application.usecases.paymentreminder.ToggleReminderStatusUseCase;
import com.reminder.financial.api.application.usecases.user.ToggleSelfUserAccountUseCase;
import com.reminder.financial.api.domains.models.reminder.PaymentReminder;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.PaymentReminderService;
import com.reminder.financial.api.domains.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
@Component
public class ToggleSelfUserAccountUseCaseImpl implements ToggleSelfUserAccountUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ToggleSelfUserAccountUseCaseImpl.class);

    private final UserService userService;

    private final PaymentReminderService paymentReminderService;

    private final ToggleReminderStatusUseCase toggleReminderStatusUseCase;


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
        logger.info("user toggled userId={} enabled={}", user.getId(), enable);
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
