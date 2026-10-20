package com.reminder.financial.api.domains.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * Conta apenas tentativas de login que falharam. O contador usa INCR + EXPIRE num único script Lua
 * (atômico) e o bloqueio é uma chave separada com TTL, que cresce com as falhas até um teto.
 */
@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;

    public static final long BLOCK_TIME_MINUTES = 5;

    public static final long BLOCK_TIME_MULTIPLIER = 2;

    public static final long MAX_BLOCK_MINUTES = 30;

    private static final long FAILURE_WINDOW_SECONDS = Duration.ofMinutes(MAX_BLOCK_MINUTES).toSeconds();

    private static final DefaultRedisScript<Long> INCR_WITH_TTL = new DefaultRedisScript<>(
            "local n = redis.call('INCR', KEYS[1]) redis.call('EXPIRE', KEYS[1], ARGV[1]) return n", Long.class);

    @Autowired
    private StringRedisTemplate redis;

    public void incrementAttempts(String email) {
        Long attempts = redis.execute(INCR_WITH_TTL, List.of(failKey(email)), String.valueOf(FAILURE_WINDOW_SECONDS));
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            Duration block = Duration.ofMinutes(calculateBlockTime(attempts));
            redis.opsForValue().set(blockKey(email), String.valueOf(System.currentTimeMillis() + block.toMillis()), block);
        }
    }

    public void resetAttempts(String email) {
        redis.delete(List.of(failKey(email), blockKey(email)));
    }

    public boolean isBlocked(String email) {
        return getBlockUntil(email) > System.currentTimeMillis();
    }

    public Long getBlockUntil(String email) {
        String value = redis.opsForValue().get(blockKey(email));
        return value == null ? 0L : Long.parseLong(value);
    }

    public int getAttempts(String email) {
        String value = redis.opsForValue().get(failKey(email));
        return value == null ? 0 : Integer.parseInt(value);
    }

    private long calculateBlockTime(long attempts) {
        return Math.min(BLOCK_TIME_MINUTES * (attempts - MAX_ATTEMPTS + 1) * BLOCK_TIME_MULTIPLIER, MAX_BLOCK_MINUTES);
    }

    private String failKey(String email) {
        return "login:fail:" + email;
    }

    private String blockKey(String email) {
        return "login:block:" + email;
    }
}
