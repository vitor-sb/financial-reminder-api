package com.reminder.financial.api.application.usecases.user.impl;

import com.reminder.financial.api.application.dtos.user.EnableUserCommand;
import com.reminder.financial.api.application.exceptions.InvalidCredentialsException;
import com.reminder.financial.api.application.exceptions.TooManyLoginAttemptsException;
import com.reminder.financial.api.application.usecases.user.EnableUserUseCase;
import com.reminder.financial.api.application.usecases.user.ToggleSelfUserAccountUseCase;
import com.reminder.financial.api.domains.models.user.User;
import com.reminder.financial.api.domains.services.LoginAttemptService;
import com.reminder.financial.api.domains.repositories.jpa.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class EnableUserUseCaseImpl implements EnableUserUseCase {

    private static final Logger logger = LoggerFactory.getLogger(EnableUserUseCaseImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private ToggleSelfUserAccountUseCase toggleSelfUserAccountUseCase;

    private volatile String dummyHash;

    @Override
    public void execute(EnableUserCommand command) {
        String email = command.email();

        if (loginAttemptService.isBlocked(email)) {
            logger.warn("enable user blocked reason=too_many_attempts");
            long waitSeconds = TimeUnit.MILLISECONDS.toSeconds(loginAttemptService.getBlockUntil(email) - System.currentTimeMillis());
            throw new TooManyLoginAttemptsException("Too many login attempts. Try again later.", Math.max(waitSeconds, 1));
        }

        User user = userRepository.findUserByEmail(email).orElse(null);
        // hash dummy iguala o custo quando o e-mail não existe
        String hash = user != null ? user.getPassword() : dummyHash();
        boolean passwordMatches = passwordEncoder.matches(command.password(), hash);

        if (user == null || !passwordMatches || Boolean.TRUE.equals(user.getAudit().getDeleted())) {
            loginAttemptService.incrementAttempts(email);
            logger.warn("enable user failed reason=invalid_credentials");
            throw new InvalidCredentialsException("Invalid email or password");
        }

        loginAttemptService.resetAttempts(email);
        toggleSelfUserAccountUseCase.execute(email, true);
    }

    private String dummyHash() {
        if (dummyHash == null) {
            dummyHash = passwordEncoder.encode("dummy-password");
        }
        return dummyHash;
    }
}
