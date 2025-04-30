package com.project.manager.finance_management.adapters.usecases.user;

import com.project.manager.finance_management.adapters.dtos.user.CreateUserDTO;
import com.project.manager.finance_management.domains.models.user.User;
import com.project.manager.finance_management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CreateUserUseCase {
    @Autowired
    private UserService service;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void execute(CreateUserDTO dto) {
        String encryptedPassword = passwordEncoder.encode(dto.password());
        User user = new User(dto.name(), dto.email(), encryptedPassword, dto.role());

        this.service.createUser(user);
    }
}
