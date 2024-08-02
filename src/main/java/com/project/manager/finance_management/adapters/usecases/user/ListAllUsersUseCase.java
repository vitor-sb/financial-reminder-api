package com.project.manager.finance_management.adapters.usecases.user;

import com.project.manager.finance_management.adapters.dtos.user.ListAllUsersResponseDTO;
import com.project.manager.finance_management.domains.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListAllUsersUseCase {
    @Autowired
    private UserService service;

    public List<ListAllUsersResponseDTO> execute() {
        var allUsers = this.service.listAllUsers();

        return allUsers.stream()
                .map(user -> new ListAllUsersResponseDTO(user.getName(), user.getEmail(), user.getRoles()))
                .toList();
    }
}
