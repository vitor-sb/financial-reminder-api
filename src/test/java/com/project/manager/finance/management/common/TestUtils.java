package com.project.manager.finance.management.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Component
public class TestUtils {

    @Value("${api.infrastructure.redis.login-attempt.max-attempts}")
    public static int maxAttempts;

    @Value("${api.infrastructure.redis.login-attempt.block-time-minutes}")
    public static long blockTimeMinutes;

    @Value("${api.infrastructure.redis.login-attempt.block-time-multiplier}")
    public static long blockTimeMultiplier;

    private static final long TIME_TOLERANCE_MS = 5;

    public static long calculateExpectedBlockUntil(long attempts) {
        long expectedBlockTimeMinutes = blockTimeMinutes * (attempts - maxAttempts + 1) * blockTimeMultiplier;
        long expectedBlockTimeMillis = expectedBlockTimeMinutes * 60000;
        return System.currentTimeMillis() + expectedBlockTimeMillis;
    }

    // TODO: Study the possibility of removing this function
    public static void assertTimeEqualsWithTolerance(long expected, long actual, String message) {
        assertTrue(actual >= (expected - TIME_TOLERANCE_MS) && actual <= (expected + TIME_TOLERANCE_MS),
                message + " (Expected: " + expected + ", Actual: " + actual + ")");
    }
}