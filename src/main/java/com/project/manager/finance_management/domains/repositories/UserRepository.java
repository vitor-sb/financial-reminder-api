package com.project.manager.finance_management.domains.repositories;

import com.project.manager.finance_management.domains.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByLogin(String login);
}
