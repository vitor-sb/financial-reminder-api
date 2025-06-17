package com.project.manager.finance.management.application.usecases.paymentreminder.impl;

import com.project.manager.finance.management.application.usecases.paymentreminder.DeleteReminderUseCase;
import com.project.manager.finance.management.domains.services.PaymentReminderService;
import com.project.manager.finance.management.domains.services.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DeleteReminderUseCaseImpl implements DeleteReminderUseCase {
    @Autowired
    private PaymentReminderService paymentReminderService;

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @Override
    @Transactional
    public void execute(String reminderId, String userEmail) {
        paymentTransactionService.cancelAndSoftDeletePendingTransaction(reminderId, userEmail);

        paymentReminderService.softDeleteReminder(reminderId, userEmail);
    }
}
