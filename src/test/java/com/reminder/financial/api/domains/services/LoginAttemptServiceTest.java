package com.reminder.financial.api.domains.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.reminder.financial.api.domains.services.LoginAttemptService.MAX_ATTEMPTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginAttemptServiceTest {

    private static final String EMAIL = "test@example.com";

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> ops;

    @InjectMocks
    private LoginAttemptService service;

    private final AtomicLong counter = new AtomicLong();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redis.opsForValue()).thenReturn(ops);
        // simula o script INCR+EXPIRE atômico do Redis
        when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenAnswer(i -> counter.incrementAndGet());
        when(redis.delete(anyCollection())).thenAnswer(i -> {
            counter.set(0);
            return 2L;
        });
    }

    @Test
    @DisplayName("Fewer than MAX_ATTEMPTS failures do not block")
    void belowLimitDoesNotBlock() {
        for (int i = 0; i < MAX_ATTEMPTS - 1; i++) service.incrementAttempts(EMAIL);

        verify(ops, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("MAX_ATTEMPTS failures block for 10 minutes")
    void limitBlocks() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) service.incrementAttempts(EMAIL);

        ArgumentCaptor<Duration> ttl = ArgumentCaptor.forClass(Duration.class);
        verify(ops).set(eq("login:block:" + EMAIL), anyString(), ttl.capture());
        assertEquals(Duration.ofMinutes(10), ttl.getValue());
    }

    @Test
    @DisplayName("Block time is capped at 30 minutes")
    void blockTimeIsCapped() {
        counter.set(50);
        service.incrementAttempts(EMAIL);

        ArgumentCaptor<Duration> ttl = ArgumentCaptor.forClass(Duration.class);
        verify(ops).set(eq("login:block:" + EMAIL), anyString(), ttl.capture());
        assertEquals(Duration.ofMinutes(30), ttl.getValue());
    }

    @Test
    @DisplayName("4 failures + 1 success resets the counter")
    void successResetsCounter() {
        for (int i = 0; i < 4; i++) service.incrementAttempts(EMAIL);
        service.resetAttempts(EMAIL);

        verify(redis).delete(List.of("login:fail:" + EMAIL, "login:block:" + EMAIL));
        service.incrementAttempts(EMAIL);
        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("20 parallel failures increment exactly 20")
    void parallelIncrements() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 20; i++) pool.submit(() -> service.incrementAttempts(EMAIL));
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(20, counter.get());
    }

    @Test
    @DisplayName("isBlocked reflects the block key")
    void isBlocked() {
        when(ops.get("login:block:" + EMAIL)).thenReturn(String.valueOf(System.currentTimeMillis() + 60000));
        assertTrue(service.isBlocked(EMAIL));

        when(ops.get("login:block:" + EMAIL)).thenReturn(null);
        assertFalse(service.isBlocked(EMAIL));
        assertEquals(0L, service.getBlockUntil(EMAIL));
    }

    @Test
    @DisplayName("getAttempts reads the failure counter")
    void getAttempts() {
        when(ops.get("login:fail:" + EMAIL)).thenReturn("2");
        assertEquals(2, service.getAttempts(EMAIL));

        when(ops.get("login:fail:" + EMAIL)).thenReturn(null);
        assertEquals(0, service.getAttempts(EMAIL));
    }
}
