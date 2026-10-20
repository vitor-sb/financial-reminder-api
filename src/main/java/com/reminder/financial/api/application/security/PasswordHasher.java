package com.reminder.financial.api.application.security;

public interface PasswordHasher {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String hash);
}
