package com.reminder.financial.api.adapters.controllers;

import com.reminder.financial.api.adapters.dtos.authentication.AuthenticationRequest;
import com.reminder.financial.api.adapters.dtos.authentication.AuthenticationResponse;
import com.reminder.financial.api.adapters.mappers.AuthenticationMapper;
import com.reminder.financial.api.application.dtos.authentication.AuthenticationCommand;
import com.reminder.financial.api.application.usecases.authentication.LoginUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationMapper authenticationMapper;

    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest dto) {
        AuthenticationCommand command = authenticationMapper.toCommand(dto);
        var result = loginUseCase.execute(command);

        AuthenticationResponse response = authenticationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }
}
