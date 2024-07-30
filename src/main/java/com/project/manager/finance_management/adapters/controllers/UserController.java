package com.project.manager.finance_management.adapters.controllers;

import com.project.manager.finance_management.adapters.dtos.user.ListAllUsersResponseDTO;
import com.project.manager.finance_management.adapters.dtos.user.RegisterDTO;
import com.project.manager.finance_management.adapters.usecases.user.ListAllUsersUseCase;
import com.project.manager.finance_management.adapters.usecases.user.RegisterUserUseCase;
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
    private RegisterUserUseCase registerUserUseCase;
    @Autowired
    private ListAllUsersUseCase listAllUsersUseCase;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterDTO dto) {
        registerUserUseCase.execute(dto);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/listAll")
    public ResponseEntity<List<ListAllUsersResponseDTO>> listAll() {
        var response = listAllUsersUseCase.execute();

        return ResponseEntity.ok(response);
    }
}
