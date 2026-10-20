package com.reminder.financial.api.application.usecases.user.impl;

import com.reminder.financial.api.application.dtos.user.CreateUserCommand;
import com.reminder.financial.api.application.usecases.user.CreateUserUseCase;
import com.reminder.financial.api.application.security.PasswordHasher;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.models.user.enums.RoleEnum;
import com.reminder.financial.api.domains.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CreateUserUseCaseImpl.class);

    private final UserService userService;

    private final PasswordHasher passwordHasher;

    public void execute(CreateUserCommand dto) {
        userService.checkEmailAvailability(dto.email());

        String encryptedPassword = passwordHasher.encode(dto.password());
        User user = new User(dto.name(), dto.email(), encryptedPassword, RoleEnum.USER);

        userService.save(user);
        logger.info("user created userId={}", user.getId());
    }
}
