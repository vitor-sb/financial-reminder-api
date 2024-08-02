package com.project.manager.finance_management.adapters.usecases.user;

import com.project.manager.finance_management.adapters.dtos.user.RegisterDTO;
import com.project.manager.finance_management.domains.models.user.User;
import com.project.manager.finance_management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RegisterUserUseCase {
    @Autowired
    private UserService service;

    public void execute(RegisterDTO dto) {
        String encryptedPassword = new BCryptPasswordEncoder().encode(dto.password());
        User user = new User(dto.name(), dto.email(), encryptedPassword, dto.role());

        this.service.createUser(user);
    }
}
