package com.project.manager.finance_management.domains.repositories.redis;

import com.project.manager.finance_management.domains.models.authentication.LoginAttempt;
import org.springframework.data.repository.CrudRepository;

public interface LoginAttemptRepository extends CrudRepository<LoginAttempt, String> {
}
