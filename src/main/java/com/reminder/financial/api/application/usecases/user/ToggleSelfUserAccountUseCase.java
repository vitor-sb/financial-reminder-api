package com.reminder.financial.api.application.usecases.user;

public interface ToggleSelfUserAccountUseCase {
    void execute(String userEmail, boolean enable);
}
