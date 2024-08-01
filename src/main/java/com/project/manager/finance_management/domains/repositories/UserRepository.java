package com.project.manager.finance_management.domains.repositories;

import com.project.manager.finance_management.domains.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<UserDetails> findByLogin(String login);
    Optional<User> findUserByLogin(String login);
}
