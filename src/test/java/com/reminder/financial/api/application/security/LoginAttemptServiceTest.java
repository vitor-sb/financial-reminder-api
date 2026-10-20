package com.reminder.financial.api.application.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.reminder.financial.api.application.security.LoginAttemptService.MAX_ATTEMPTS;
import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private static final String EMAIL = "test@example.com";

    private final InMemoryLoginAttemptStore store = new InMemoryLoginAttemptStore();

    private final LoginAttemptService service = new LoginAttemptService(store);

    @Test
    @DisplayName("Fewer than MAX_ATTEMPTS failures do not block")
    void belowLimitDoesNotBlock() {
        for (int i = 0; i < MAX_ATTEMPTS - 1; i++) service.incrementAttempts(EMAIL);

        assertTrue(store.blocks.isEmpty());
        assertFalse(service.isBlocked(EMAIL));
    }

    @Test
    @DisplayName("MAX_ATTEMPTS failures block for 10 minutes")
    void limitBlocks() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) service.incrementAttempts(EMAIL);

        assertEquals(Duration.ofMinutes(10), store.blockTtls.get(EMAIL));
        assertTrue(service.isBlocked(EMAIL));
    }

    @Test
    @DisplayName("Block time is capped at 30 minutes")
    void blockTimeIsCapped() {
        store.failures.put(EMAIL, 49L);
        service.incrementAttempts(EMAIL);

        assertEquals(Duration.ofMinutes(30), store.blockTtls.get(EMAIL));
    }

    @Test
    @DisplayName("4 failures + 1 success resets the counter")
    void successResetsCounter() {
        for (int i = 0; i < 4; i++) service.incrementAttempts(EMAIL);
        service.resetAttempts(EMAIL);

        assertEquals(0, service.getAttempts(EMAIL));
        service.incrementAttempts(EMAIL);
        assertEquals(1, service.getAttempts(EMAIL));
    }

    @Test
    @DisplayName("20 parallel failures increment exactly 20")
    void parallelIncrements() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 20; i++) pool.submit(() -> service.incrementAttempts(EMAIL));
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(20, service.getAttempts(EMAIL));
    }

    @Test
    @DisplayName("isBlocked reflects the block")
    void isBlocked() {
        store.block(EMAIL, System.currentTimeMillis() + 60000, Duration.ofMinutes(1));
        assertTrue(service.isBlocked(EMAIL));

        store.clear(EMAIL);
        assertFalse(service.isBlocked(EMAIL));
        assertEquals(0L, service.getBlockUntil(EMAIL));
    }

    @Test
    @DisplayName("getAttempts reads the failure counter")
    void getAttempts() {
        store.failures.put(EMAIL, 2L);
        assertEquals(2, service.getAttempts(EMAIL));

        store.clear(EMAIL);
        assertEquals(0, service.getAttempts(EMAIL));
    }
}
