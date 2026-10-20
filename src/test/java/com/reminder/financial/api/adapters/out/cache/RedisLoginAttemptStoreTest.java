package com.reminder.financial.api.adapters.out.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedisLoginAttemptStoreTest {

    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);

    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> ops = mock(ValueOperations.class);

    private final RedisLoginAttemptStore store = new RedisLoginAttemptStore(redis);

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(ops);
    }

    @Test
    @DisplayName("Increment runs one Lua script doing INCR + EXPIRE with the window as TTL")
    void incrementIsAtomicWithTtl() {
        when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(3L);

        assertEquals(3L, store.incrementFailures("a@b.c", Duration.ofMinutes(30)));

        ArgumentCaptor<RedisScript<Long>> script = ArgumentCaptor.forClass(RedisScript.class);
        verify(redis).execute(script.capture(), eq(List.of("login:fail:a@b.c")), eq("1800"));
        String lua = script.getValue().getScriptAsString();
        assertTrue(lua.contains("INCR") && lua.contains("EXPIRE"));
        verifyNoMoreInteractions(redis);
    }

    @Test
    @DisplayName("IP increment uses the ip key and its own window")
    void ipIncrement() {
        store.incrementIpFailures("1.2.3.4", Duration.ofSeconds(900));

        verify(redis).execute(any(RedisScript.class), eq(List.of("login:ip:1.2.3.4")), eq("900"));
    }

    @Test
    @DisplayName("Block, read and clear use the block/fail keys; missing keys read as 0")
    void blockReadClear() {
        store.block("a@b.c", 123L, Duration.ofMinutes(5));
        verify(ops).set("login:block:a@b.c", "123", Duration.ofMinutes(5));

        when(ops.get("login:fail:a@b.c")).thenReturn(null);
        assertEquals(0L, store.failures("a@b.c"));
        when(ops.get("login:block:a@b.c")).thenReturn("123");
        assertEquals(123L, store.blockUntil("a@b.c"));

        store.clear("a@b.c");
        verify(redis).delete(List.of("login:fail:a@b.c", "login:block:a@b.c"));
    }
}
