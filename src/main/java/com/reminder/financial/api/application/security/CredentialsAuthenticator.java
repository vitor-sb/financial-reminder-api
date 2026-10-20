package com.reminder.financial.api.application.security;

import com.reminder.financial.api.domains.models.user.User;

public interface CredentialsAuthenticator {

    /** @throws com.reminder.financial.api.domains.exceptions.InvalidCredentialsException se a autenticação falhar por qualquer motivo */
    User authenticate(String email, String password);
}
