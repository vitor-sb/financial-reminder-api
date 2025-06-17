package com.project.manager.finance.management.adapters.controllers;

import com.project.manager.finance.management.adapters.dtos.authentication.AuthenticationRequest;
import com.project.manager.finance.management.adapters.dtos.authentication.AuthenticationResponse;
import com.project.manager.finance.management.adapters.mappers.AuthenticationMapper;
import com.project.manager.finance.management.application.dtos.authentication.AuthenticationCommand;
import com.project.manager.finance.management.application.usecases.authentication.LoginUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@Tag(name = "Autenticação", description = "Endpoint para login e geração de token de acesso.")
public class AuthenticationController {

    @Autowired
    private AuthenticationMapper authenticationMapper;

    @Autowired
    private LoginUseCase loginUseCase;

    @PostMapping("/login")
    @Operation(summary = "Realiza o login do usuário", description = "Autentica um usuário com e-mail e senha e retorna um token JWT em caso de sucesso.")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest dto) {
        AuthenticationCommand command = authenticationMapper.toCommand(dto);
        var result = loginUseCase.execute(command);

        AuthenticationResponse response = authenticationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }
}
