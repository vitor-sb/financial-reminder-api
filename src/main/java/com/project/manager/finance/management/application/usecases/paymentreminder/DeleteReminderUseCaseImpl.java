package com.project.manager.finance.management.application.usecases.paymentreminder;

import com.project.manager.finance.management.domains.services.PaymentReminderService;
import org.springframework.beans.factory.annotation.Autowired;

public class DeleteReminderUseCaseImpl implements DeleteReminderUseCase{
    @Autowired
    private PaymentReminderService paymentReminderService;

    @Override
    public void execute(String reminderId, String userEmail) {
        paymentReminderService.softDeleteReminder(reminderId, userEmail);
    }

}
