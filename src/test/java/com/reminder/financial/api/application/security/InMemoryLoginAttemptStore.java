package com.reminder.financial.api.application.security;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Fake em memória da porta; TTLs são registrados mas não expiram. */
public class InMemoryLoginAttemptStore implements LoginAttemptStore {

    public final Map<String, Long> failures = new ConcurrentHashMap<>();
    public final Map<String, Long> blocks = new ConcurrentHashMap<>();
    public final Map<String, Duration> blockTtls = new ConcurrentHashMap<>();
    public final Map<String, Long> ips = new ConcurrentHashMap<>();

    @Override
    public long incrementFailures(String email, Duration window) {
        return failures.merge(email, 1L, Long::sum);
    }

    @Override
    public long failures(String email) {
        return failures.getOrDefault(email, 0L);
    }

    @Override
    public void block(String email, long untilEpochMillis, Duration ttl) {
        blocks.put(email, untilEpochMillis);
        blockTtls.put(email, ttl);
    }

    @Override
    public long blockUntil(String email) {
        return blocks.getOrDefault(email, 0L);
    }

    @Override
    public void clear(String email) {
        failures.remove(email);
        blocks.remove(email);
    }

    @Override
    public long incrementIpFailures(String ip, Duration window) {
        return ips.merge(ip, 1L, Long::sum);
    }

    @Override
    public long ipFailures(String ip) {
        return ips.getOrDefault(ip, 0L);
    }

    @Override
    public long ipTtlSeconds(String ip) {
        return -1;
    }
}
