package com.project.manager.finance_management.adapters.usecases.authentication;

import com.project.manager.finance_management.adapters.dtos.authentication.AuthenticationDTO;
import com.project.manager.finance_management.adapters.dtos.authentication.LoginResponseDTO;
import com.project.manager.finance_management.domains.models.user.User;
import com.project.manager.finance_management.infrastructure.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class LoginUseCase {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenProvider tokenProvider;

    public LoginResponseDTO execute(AuthenticationDTO dto) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.login(), dto.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = this.tokenProvider.generateToken((User) auth.getPrincipal());

        return new LoginResponseDTO(token);
    }
}
