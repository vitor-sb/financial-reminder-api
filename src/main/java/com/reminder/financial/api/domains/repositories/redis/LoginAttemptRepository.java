package com.reminder.financial.api.domains.repositories.redis;

import com.reminder.financial.api.domains.models.authentication.LoginAttempt;
import org.springframework.data.repository.CrudRepository;

public interface LoginAttemptRepository extends CrudRepository<LoginAttempt, String> {
}
