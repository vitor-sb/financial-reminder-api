package com.project.manager.finance.management.application.usecases.user;

public interface ToggleSelfUserAccountUseCase {
    void execute(String userEmail, boolean enable);
}
