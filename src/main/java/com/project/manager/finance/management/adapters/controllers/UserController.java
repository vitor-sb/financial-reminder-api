package com.project.manager.finance.management.adapters.controllers;

import com.project.manager.finance.management.adapters.dtos.user.ListUsersResponseDTO;
import com.project.manager.finance.management.adapters.dtos.user.CreateUserDTO;
import com.project.manager.finance.management.adapters.usecases.user.ListUsersUseCase;
import com.project.manager.finance.management.adapters.usecases.user.CreateUserUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private CreateUserUseCase createUserUseCase;
    @Autowired
    private ListUsersUseCase listUsersUseCase;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateUserDTO dto) {
        createUserUseCase.execute(dto);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ADMIN_PERMISSION')")
    @GetMapping("/list")
    public ResponseEntity<List<ListUsersResponseDTO>> listAll() {
        var response = listUsersUseCase.execute();

        return ResponseEntity.ok(response);
    }
}
