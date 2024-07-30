package com.project.manager.finance_management.adapters.controllers;

import com.project.manager.finance_management.adapters.dtos.user.RegisterDTO;
import com.project.manager.finance_management.adapters.usecases.user.RegisterUserUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegisterDTO dto) {
        registerUserUseCase.execute(dto);

        return ResponseEntity.ok().build();
    }
}
