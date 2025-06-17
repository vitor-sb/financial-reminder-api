package com.reminder.financial.api.application.usecases.paymentreminder;

public interface DeletePaymentReminderUseCase {
    void execute(String reminderId, String userEmail);
}
