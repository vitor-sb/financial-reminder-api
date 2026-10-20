package com.reminder.financial.api.adapters.out.cache;

import lombok.RequiredArgsConstructor;

import com.reminder.financial.api.application.security.LoginAttemptStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Component
public class RedisLoginAttemptStore implements LoginAttemptStore {

    private static final DefaultRedisScript<Long> INCR_WITH_TTL = new DefaultRedisScript<>(
            "local n = redis.call('INCR', KEYS[1]) redis.call('EXPIRE', KEYS[1], ARGV[1]) return n", Long.class);

    private final StringRedisTemplate redis;

    @Override
    public long incrementFailures(String email, Duration window) {
        return incr(failKey(email), window);
    }

    @Override
    public long failures(String email) {
        return read(failKey(email));
    }

    @Override
    public void block(String email, long untilEpochMillis, Duration ttl) {
        redis.opsForValue().set(blockKey(email), String.valueOf(untilEpochMillis), ttl);
    }

    @Override
    public long blockUntil(String email) {
        return read(blockKey(email));
    }

    @Override
    public void clear(String email) {
        redis.delete(List.of(failKey(email), blockKey(email)));
    }

    @Override
    public long incrementIpFailures(String ip, Duration window) {
        return incr(ipKey(ip), window);
    }

    @Override
    public long ipFailures(String ip) {
        return read(ipKey(ip));
    }

    @Override
    public long ipTtlSeconds(String ip) {
        Long ttl = redis.getExpire(ipKey(ip));
        return ttl == null ? -1 : ttl;
    }

    private long incr(String key, Duration window) {
        Long n = redis.execute(INCR_WITH_TTL, List.of(key), String.valueOf(window.toSeconds()));
        return n == null ? 0 : n;
    }

    private long read(String key) {
        String value = redis.opsForValue().get(key);
        return value == null ? 0 : Long.parseLong(value);
    }

    private String failKey(String email) {
        return "login:fail:" + email;
    }

    private String ipKey(String ip) {
        return "login:ip:" + ip;
    }

    private String blockKey(String email) {
        return "login:block:" + email;
    }
}
