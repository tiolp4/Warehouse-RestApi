package ru.warehouse.api.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.warehouse.api.dto.Dtos.LoginRequest;
import ru.warehouse.api.dto.Dtos.LoginResponse;
import ru.warehouse.api.service.AuthService;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }
}
