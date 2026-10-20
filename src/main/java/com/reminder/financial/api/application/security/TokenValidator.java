package com.reminder.financial.api.application.security;

public interface TokenValidator {

    String validateToken(String token);
}
