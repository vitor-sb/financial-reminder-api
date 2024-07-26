package com.project.manager.finance_management.controllers;

import com.project.manager.finance_management.domains.user.AuthenticationDTO;
import com.project.manager.finance_management.domains.user.LoginResponseDTO;
import com.project.manager.finance_management.domains.user.RegisterDTO;
import com.project.manager.finance_management.domains.user.User;
import com.project.manager.finance_management.infrastructure.security.TokenProvider;
import com.project.manager.finance_management.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository repository;
    @Autowired
    private TokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegisterDTO dto) {
        if (this.repository.findByLogin(dto.login()) != null)
            return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(dto.password());
        User user = new User(dto.name(), dto.login(), encryptedPassword, dto.role());

        this.repository.save(user);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO dto) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.login(), dto.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = tokenProvider.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}
