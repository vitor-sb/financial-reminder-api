package com.reminder.financial.api.application.security;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Conta apenas tentativas de login que falharam. O bloqueio tem TTL que cresce com as falhas até um teto;
 * o armazenamento (e a atomicidade de incremento + TTL) fica na porta {@link LoginAttemptStore}.
 */
@RequiredArgsConstructor
@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;

    public static final long BLOCK_TIME_MINUTES = 5;

    public static final long BLOCK_TIME_MULTIPLIER = 2;

    public static final long MAX_BLOCK_MINUTES = 30;

    private static final long FAILURE_WINDOW_SECONDS = Duration.ofMinutes(MAX_BLOCK_MINUTES).toSeconds();

    private final LoginAttemptStore store;

    @Value("${login.ip-limit.max-failures:20}")
    private long ipMaxFailures;

    @Value("${login.ip-limit.window-seconds:900}")
    private long ipWindowSeconds;

    public void incrementAttempts(String email) {
        long attempts = store.incrementFailures(email, Duration.ofSeconds(FAILURE_WINDOW_SECONDS));
        if (attempts >= MAX_ATTEMPTS) {
            Duration block = Duration.ofMinutes(calculateBlockTime(attempts));
            store.block(email, System.currentTimeMillis() + block.toMillis(), block);
        }
    }

    public void incrementIpFailures(String ip) {
        store.incrementIpFailures(ip, Duration.ofSeconds(ipWindowSeconds));
    }

    public boolean isIpBlocked(String ip) {
        return store.ipFailures(ip) >= ipMaxFailures;
    }

    public long ipRetryAfterSeconds(String ip) {
        long ttl = store.ipTtlSeconds(ip);
        return ttl < 1 ? ipWindowSeconds : ttl;
    }

    public void resetAttempts(String email) {
        store.clear(email);
    }

    public boolean isBlocked(String email) {
        return getBlockUntil(email) > System.currentTimeMillis();
    }

    public Long getBlockUntil(String email) {
        return store.blockUntil(email);
    }

    public int getAttempts(String email) {
        return (int) store.failures(email);
    }

    private long calculateBlockTime(long attempts) {
        return Math.min(BLOCK_TIME_MINUTES * (attempts - MAX_ATTEMPTS + 1) * BLOCK_TIME_MULTIPLIER, MAX_BLOCK_MINUTES);
    }
}
