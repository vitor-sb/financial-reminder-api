package com.project.manager.finance.management.adapters.controllers;

import com.project.manager.finance.management.adapters.dtos.user.CreateUserRequest;
import com.project.manager.finance.management.adapters.dtos.user.ListUsersSingleResponse;
import com.project.manager.finance.management.adapters.mappers.UserMapper;
import com.project.manager.finance.management.application.dtos.CreateUserCommand;
import com.project.manager.finance.management.application.usecases.user.ListUsersUseCase;
import com.project.manager.finance.management.application.usecases.user.CreateUserUseCase;
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
    private UserMapper userMapper;
    @Autowired
    private CreateUserUseCase createUserUseCase;
    @Autowired
    private ListUsersUseCase listUsersUseCase;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateUserRequest dto) {
        CreateUserCommand command = userMapper.toCommand(dto);

        createUserUseCase.execute(command);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ADMIN_PERMISSION')")
    @GetMapping("/list")
    public ResponseEntity<List<ListUsersSingleResponse>> listAll() {
        var result = listUsersUseCase.execute();

        List<ListUsersSingleResponse> response = userMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
