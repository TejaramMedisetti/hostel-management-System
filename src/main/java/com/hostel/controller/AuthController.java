package com.hostel.controller;

import com.hostel.dto.AuthDtos.LoginRequest;
import com.hostel.dto.AuthDtos.LoginResponse;
import com.hostel.model.User;
import com.hostel.repository.UserRepository;
import com.hostel.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final UserRepository users;
    private final JwtService jwt;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        User u = users.findByUsername(req.username()).orElseThrow();
        return new LoginResponse(jwt.generate(u.getUsername(), u.getRole().name()), u.getUsername(), u.getRole().name());
    }
}
