package com.project.manager.finance_management.common.mocks;

import com.project.manager.finance_management.domains.models.user.Role;
import com.project.manager.finance_management.domains.models.user.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;


public class UserMock {

    public static User createAdminUserMock(Optional<String> name, Optional<String> email, Optional<String> password) {
        return new User(
                name.orElse("User Admin name"),
                email.orElse("admin@example.com"),
                new BCryptPasswordEncoder().encode(password.orElse("securepassword")),
                Role.ADMIN
        );
    }

    public static User createNormalUserMock(Optional<String> name, Optional<String> email, Optional<String> password) {
        return new User(
                name.orElse("Normal User name"),
                email.orElse("user@example.com"),
                new BCryptPasswordEncoder().encode(password.orElse("password")),
                Role.USER
        );
    }
}
