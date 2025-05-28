package com.project.manager.finance.management.adapters.controllers;

import com.project.manager.finance.management.adapters.dtos.authentication.AuthenticationDTO;
import com.project.manager.finance.management.adapters.dtos.authentication.LoginResponseDTO;
import com.project.manager.finance.management.adapters.usecases.authentication.LoginUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthenticationController {
    @Autowired
    private LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO dto) {
        var response = loginUseCase.execute(dto);

        return ResponseEntity.ok(response);
    }
}
