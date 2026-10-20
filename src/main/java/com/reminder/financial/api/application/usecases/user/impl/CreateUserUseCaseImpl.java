package com.reminder.financial.api.application.usecases.user.impl;

import com.reminder.financial.api.application.dtos.user.CreateUserCommand;
import com.reminder.financial.api.application.usecases.user.CreateUserUseCase;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.models.user.enums.RoleEnum;
import com.reminder.financial.api.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void execute(CreateUserCommand dto) {
        userService.checkEmailAvailability(dto.email());

        String encryptedPassword = passwordEncoder.encode(dto.password());
        User user = new User(dto.name(), dto.email(), encryptedPassword, RoleEnum.USER);

        userService.save(user);
    }
}
