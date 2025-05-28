package com.project.manager.finance.management.domains.services;

import com.project.manager.finance.management.domains.models.authentication.LoginAttempt;
import com.project.manager.finance.management.domains.repositories.redis.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Value("${infrastructure.redis.login-attempt.max-attempts}")
    private int maxAttempts;

    @Value("${infrastructure.redis.login-attempt.block-time-minutes}")
    private long blockTimeMinutes;

    @Value("${infrastructure.redis.login-attempt.block-time-multiplier}")
    private long blockTimeMultiplier;


    public void incrementAttempts(String email) {
        LoginAttempt loginAttempt = loginAttemptRepository.findById(email).orElseGet(() -> new LoginAttempt(email));
        loginAttempt.setAttempts(loginAttempt.getAttempts() + 1);

        if (loginAttempt.getAttempts() >= maxAttempts) {
            blockUntil(loginAttempt);
        }

        loginAttemptRepository.save(loginAttempt);
    }

    public void resetAttempts(String email) {
        loginAttemptRepository.deleteById(email);
    }

    public boolean isBlocked(String email) {
        LoginAttempt loginAttempt = loginAttemptRepository.findById(email).orElse(null);
        if (loginAttempt == null) {
            return false;
        }
        return loginAttempt.getBlockUntil() != null && loginAttempt.getBlockUntil() > System.currentTimeMillis();
    }

    public void blockUntil(String email) {
        LoginAttempt loginAttempt = loginAttemptRepository.findById(email).orElseGet(() -> new LoginAttempt(email));
        blockUntil(loginAttempt);
        loginAttemptRepository.save(loginAttempt);
    }

    public void blockUntil(LoginAttempt loginAttempt) {
        long blockTime = calculateBlockTime(loginAttempt.getAttempts());
        loginAttempt.setBlockUntil(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(blockTime));
    }

    public Long getBlockUntil(String email) {
        LoginAttempt loginAttempt = loginAttemptRepository.findById(email).orElse(null);
        if (loginAttempt == null) {
            return 0L;
        }
        return loginAttempt.getBlockUntil();
    }

    public int getAttempts(String email) {
        LoginAttempt loginAttempt = loginAttemptRepository.findById(email).orElseGet(() -> new LoginAttempt(email));
        return loginAttempt.getAttempts();
    }

    private long calculateBlockTime(long attempts) {
        return blockTimeMinutes * (attempts - maxAttempts + 1) * blockTimeMultiplier;
    }
}