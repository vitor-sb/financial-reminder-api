package com.reminder.financial.api.application.security;

import com.reminder.financial.api.domains.models.user.User;

public interface TokenIssuer {

    String issue(User user);
}
