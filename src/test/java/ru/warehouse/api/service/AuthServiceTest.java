package ru.warehouse.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.warehouse.api.dto.Dtos.LoginRequest;
import ru.warehouse.api.dto.Dtos.LoginResponse;
import ru.warehouse.api.entity.User;
import ru.warehouse.api.exception.ApiException;
import ru.warehouse.api.repository.UserRepository;
import ru.warehouse.api.security.JwtService;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepo;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;
    @InjectMocks AuthService service;

    private User existingUser;

    @BeforeEach
    void setUp() throws Exception {
        existingUser = new User();
        setField(existingUser, "id", UUID.randomUUID());
        existingUser.setUsername("ivanov");
        existingUser.setPasswordHash("$2a$10$hash");
        existingUser.setRole(User.Role.MANAGER);
        existingUser.setFullName("Иванов Сергей");
    }

    @Test
    void login_returnsTokenAndUserOnSuccess() {
        when(userRepo.findByUsername("ivanov")).thenReturn(Optional.of(existingUser));
        when(encoder.matches("password123", "$2a$10$hash")).thenReturn(true);
        when(jwt.issue("ivanov", "MANAGER", "Иванов Сергей"))
                .thenReturn(new JwtService.IssuedToken("jwt-token", 99999L));

        LoginResponse resp = service.login(new LoginRequest("ivanov", "password123"));

        assertEquals("jwt-token", resp.token());
        assertEquals(99999L, resp.expiresAt());
        assertEquals("MANAGER", resp.user().role());
        assertEquals("Иванов Сергей", resp.user().fullName());
    }

    @Test
    void login_throwsUnauthorizedWhenUserNotFound() {
        when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> service.login(new LoginRequest("ghost", "anything")));
        assertEquals(401, ex.status().value());
        verifyNoInteractions(jwt);
    }

    @Test
    void login_throwsUnauthorizedWhenPasswordWrong() {
        when(userRepo.findByUsername("ivanov")).thenReturn(Optional.of(existingUser));
        when(encoder.matches("badpass", "$2a$10$hash")).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class,
                () -> service.login(new LoginRequest("ivanov", "badpass")));
        assertEquals(401, ex.status().value());
        verifyNoInteractions(jwt);
    }

    @Test
    void login_doesNotLeakPasswordHashInResponse() {
        when(userRepo.findByUsername("ivanov")).thenReturn(Optional.of(existingUser));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwt.issue(anyString(), anyString(), anyString()))
                .thenReturn(new JwtService.IssuedToken("t", 0L));

        LoginResponse resp = service.login(new LoginRequest("ivanov", "x"));

        assertNotNull(resp.user());
        // UserDto record fields: id, username, fullName, role — no password field.
        assertEquals(4, resp.user().getClass().getRecordComponents().length);
    }

    @Test
    void login_callsJwtServiceWithRoleAndFullName() {
        when(userRepo.findByUsername("ivanov")).thenReturn(Optional.of(existingUser));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwt.issue(anyString(), anyString(), anyString()))
                .thenReturn(new JwtService.IssuedToken("t", 0L));

        service.login(new LoginRequest("ivanov", "x"));

        verify(jwt).issue("ivanov", "MANAGER", "Иванов Сергей");
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
