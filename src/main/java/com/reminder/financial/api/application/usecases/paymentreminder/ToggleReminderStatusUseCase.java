package com.reminder.financial.api.application.usecases.paymentreminder;

public interface ToggleReminderStatusUseCase {
    void execute(String reminderId, boolean enable, String userEmail);
}
