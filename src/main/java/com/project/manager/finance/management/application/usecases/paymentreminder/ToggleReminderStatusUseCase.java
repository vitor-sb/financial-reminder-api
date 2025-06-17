package com.project.manager.finance.management.application.usecases.paymentreminder;

public interface ToggleReminderStatusUseCase {
    void execute(String reminderId, boolean enable, String userEmail);
}
