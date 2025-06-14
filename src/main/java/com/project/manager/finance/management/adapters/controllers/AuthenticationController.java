package com.project.manager.finance.management.adapters.controllers;

import com.project.manager.finance.management.adapters.dtos.authentication.AuthenticationRequest;
import com.project.manager.finance.management.adapters.dtos.authentication.AuthenticationResponse;
import com.project.manager.finance.management.adapters.mappers.AuthenticationMapper;
import com.project.manager.finance.management.application.dtos.authentication.AuthenticationCommand;
import com.project.manager.finance.management.application.usecases.authentication.LoginUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationMapper authenticationMapper;

    @Autowired
    private LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest dto) {
        AuthenticationCommand command = authenticationMapper.toCommand(dto);
        var result = loginUseCase.execute(command);

        AuthenticationResponse response = authenticationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }
}
