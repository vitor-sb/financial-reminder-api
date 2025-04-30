package com.project.manager.finance_management.adapters.usecases.user;

import com.project.manager.finance_management.adapters.dtos.user.ListUsersResponseDTO;
import com.project.manager.finance_management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListUsersUseCase {
    @Autowired
    private UserService service;

    public List<ListUsersResponseDTO> execute() {
        var users = this.service.listUsers();

        return users.stream()
                .map(user -> new ListUsersResponseDTO(user.getName(), user.getEmail(), user.getRole()))
                .toList();
    }
}
