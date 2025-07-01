package com.reminder.financial.api.adapters.controllers;

import com.reminder.financial.api.adapters.dtos.authentication.AuthenticationRequest;
import com.reminder.financial.api.adapters.dtos.authentication.AuthenticationResponse;
import com.reminder.financial.api.adapters.mappers.AuthenticationMapper;
import com.reminder.financial.api.application.dtos.authentication.AuthenticationCommand;
import com.reminder.financial.api.application.usecases.authentication.LoginUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@Tag(name = "Authentication", description = "Responsible for managing authentication and API access.")
public class AuthenticationController {

    @Autowired
    private AuthenticationMapper authenticationMapper;

    @Autowired
    private LoginUseCase loginUseCase;

    @PostMapping("/login")
    @Operation(summary = "Validates user credentials and returns an access token")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest dto) {
        AuthenticationCommand command = authenticationMapper.toCommand(dto);
        var result = loginUseCase.execute(command);

        AuthenticationResponse response = authenticationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }
}
