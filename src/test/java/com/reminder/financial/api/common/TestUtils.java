package com.reminder.financial.api.common;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TestUtils {
    public static long calculateExpectedBlockUntil(
            int maxAttempts, long blockTimeMinutes, long blockTimeMultiplier, long attempts
    ) {
        long expectedBlockTimeMinutes = blockTimeMinutes * (attempts - maxAttempts + 1) * blockTimeMultiplier;
        long expectedBlockTimeMillis = expectedBlockTimeMinutes * 60000;
        return System.currentTimeMillis() + expectedBlockTimeMillis;
    }

    // TODO: Study the possibility of removing this function
    public static void assertTimeEqualsWithTolerance(long expected, long actual, String message) {
        var TIME_TOLERANCE_MS = 5;

        assertTrue(actual >= (expected - TIME_TOLERANCE_MS) && actual <= (expected + TIME_TOLERANCE_MS),
                message + " (Expected: " + expected + ", Actual: " + actual + ")");
    }
}