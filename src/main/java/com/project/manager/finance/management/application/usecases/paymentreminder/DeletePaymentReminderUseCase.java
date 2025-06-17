package com.project.manager.finance.management.application.usecases.paymentreminder;

public interface DeletePaymentReminderUseCase {
    void execute(String reminderId, String userEmail);
}
