package com.project.manager.finance.management.application.usecases.paymentreminder;

public interface DeleteReminderUseCase {
    void execute(String reminderId, String userEmail);
}
