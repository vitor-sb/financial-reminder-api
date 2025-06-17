package com.project.manager.finance.management.application.usecases.paymentreminder.impl;

import com.project.manager.finance.management.application.usecases.paymentreminder.ToggleReminderStatusUseCase;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ToggleReminderStatusUseCaseImpl implements ToggleReminderStatusUseCase {
    @Autowired
    private PaymentReminderService paymentReminderService;

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @Override
    public void execute(String reminderId, boolean enable, String userEmail) {
        paymentReminderService.toggleReminderStatus(reminderId, enable, userEmail);

        paymentTransactionService.togglePendingTransactionStatus(reminderId, enable, userEmail);
    }
}
