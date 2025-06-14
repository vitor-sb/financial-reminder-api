package com.project.manager.finance.management.domains.services;

import com.project.manager.finance.management.application.exceptions.TooManyLoginAttemptsException;
import com.project.manager.finance.management.domains.models.authentication.LoginAttempt;
import com.project.manager.finance.management.domains.repositories.redis.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    public static final int MAX_ATTEMPTS = 5;

    public static final long BLOCK_TIME_MINUTES = 5;

    public static final long BLOCK_TIME_MULTIPLIER = 2;

    public static final long THROTTLE_TIME_SECONDS = 15;


    public void checkAndRecordRequestRate(String email) {
        long currentTime = System.currentTimeMillis();
        LoginAttempt attempt = loginAttemptRepository.findById(email).orElseGet(() -> new LoginAttempt(email));

        if (attempt.getLastAttemptTimestamp() != null) {
            long timeSinceLastAttempt = currentTime - attempt.getLastAttemptTimestamp();
            long throttleTimeMillis = TimeUnit.SECONDS.toMillis(THROTTLE_TIME_SECONDS);

            if (timeSinceLastAttempt < throttleTimeMillis) {
                long waitTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(throttleTimeMillis - timeSinceLastAttempt);
                throw new TooManyLoginAttemptsException("Muitas tentativas em um curto período. Por favor, aguarde " + waitTimeSeconds + " segundos.");
            }
        }

        attempt.setLastAttemptTimestamp(currentTime);
        loginAttemptRepository.save(attempt);
    }

    public void incrementAttempts(String email) {
        LoginAttempt loginAttempt = loginAttemptRepository.findById(email).orElseGet(() -> new LoginAttempt(email));
        loginAttempt.setAttempts(loginAttempt.getAttempts() + 1);

        if (loginAttempt.getAttempts() >= MAX_ATTEMPTS) {
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
        return BLOCK_TIME_MINUTES * (attempts - MAX_ATTEMPTS + 1) * BLOCK_TIME_MULTIPLIER;
    }
}