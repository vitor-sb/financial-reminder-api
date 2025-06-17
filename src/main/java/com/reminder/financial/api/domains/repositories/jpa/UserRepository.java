package com.reminder.financial.api.domains.repositories.jpa;

import com.reminder.financial.api.domains.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    Optional<User> findUserByEmailAndAuditEnabledTrueAndAuditDeletedFalse(String email);
    Optional<User> findUserByEmail(String email);
}
