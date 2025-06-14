package com.project.manager.finance.management.domains.models.authentication;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@RedisHash("loginAttempts")
@Data
@NoArgsConstructor
public class LoginAttempt {

    @Id
    private String email;

    private int attempts;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long blockUntil;

    private Long lastAttemptTimestamp;

    public LoginAttempt(String email) {
        this.email = email;
        this.attempts = 0;
    }
}