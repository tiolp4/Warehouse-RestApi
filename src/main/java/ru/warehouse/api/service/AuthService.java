package ru.warehouse.api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.warehouse.api.dto.Dtos.LoginRequest;
import ru.warehouse.api.dto.Dtos.LoginResponse;
import ru.warehouse.api.dto.Dtos.UserDto;
import ru.warehouse.api.entity.User;
import ru.warehouse.api.exception.ApiException;
import ru.warehouse.api.repository.UserRepository;
import ru.warehouse.api.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository userRepo, PasswordEncoder encoder, JwtService jwt) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public LoginResponse login(LoginRequest req) {
        User u = userRepo.findByUsername(req.username())
                .orElseThrow(() -> ApiException.unauthorized("Invalid credentials"));
        if (!encoder.matches(req.password(), u.getPasswordHash()))
            throw ApiException.unauthorized("Invalid credentials");
        var t = jwt.issue(u.getUsername(), u.getRole().name(), u.getFullName());
        return new LoginResponse(t.token(), t.expiresAtEpochMs(),
                new UserDto(u.getId(), u.getUsername(), u.getFullName(), u.getRole().name()));
    }
}
