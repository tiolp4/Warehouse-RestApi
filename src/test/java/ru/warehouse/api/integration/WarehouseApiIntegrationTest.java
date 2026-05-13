package ru.warehouse.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.warehouse.api.entity.User;
import ru.warehouse.api.repository.UserRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WarehouseApiIntegrationTest {

    @Autowired UserRepository userRepo;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper mapper;
    @LocalServerPort int port;

    private static String managerToken;
    private static String pickerToken;

    private HttpClient http;
    private String base;

    @BeforeEach
    void setUp() {
        http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        base = "http://localhost:" + port + "/api";
        if (userRepo.findByUsername("it-manager").isEmpty()) {
            User m = new User();
            m.setUsername("it-manager");
            m.setPasswordHash(encoder.encode("secret"));
            m.setRole(User.Role.MANAGER);
            m.setFullName("Менеджер для тестов");
            userRepo.save(m);
        }
        if (userRepo.findByUsername("it-picker").isEmpty()) {
            User p = new User();
            p.setUsername("it-picker");
            p.setPasswordHash(encoder.encode("secret"));
            p.setRole(User.Role.PICKER);
            p.setFullName("Сборщик для тестов");
            userRepo.save(p);
        }
    }

    @Test @Order(1)
    void login_validCredentials_returns200AndToken() throws Exception {
        HttpResponse<String> r = post("/v1/auth/login",
                "{\"username\":\"it-manager\",\"password\":\"secret\"}", null);

        assertEquals(200, r.statusCode());
        JsonNode body = mapper.readTree(r.body());
        assertFalse(body.get("token").asText().isBlank());
        assertEquals("MANAGER", body.get("user").get("role").asText());
        managerToken = body.get("token").asText();
    }

    @Test @Order(2)
    void login_invalidCredentials_returns401() throws Exception {
        HttpResponse<String> r = post("/v1/auth/login",
                "{\"username\":\"it-manager\",\"password\":\"WRONG\"}", null);
        assertEquals(401, r.statusCode());
    }

    @Test @Order(3)
    void anyEndpoint_withoutToken_isDenied() throws Exception {
        HttpResponse<String> r = get("/v1/suppliers", null);
        assertTrue(r.statusCode() == 401 || r.statusCode() == 403,
                "expected 401 or 403 but got " + r.statusCode());
    }

    @Test @Order(4)
    void getSuppliers_withManagerToken_returns200() throws Exception {
        if (managerToken == null) login_validCredentials_returns200AndToken();
        HttpResponse<String> r = get("/v1/suppliers", managerToken);
        assertEquals(200, r.statusCode());
        assertTrue(r.body().startsWith("["));
    }

    @Test @Order(5)
    void shipmentsEndpoint_forbiddenForPicker() throws Exception {
        HttpResponse<String> login = post("/v1/auth/login",
                "{\"username\":\"it-picker\",\"password\":\"secret\"}", null);
        assertEquals(200, login.statusCode());
        pickerToken = mapper.readTree(login.body()).get("token").asText();

        HttpResponse<String> r = get("/v1/shipments", pickerToken);
        assertEquals(403, r.statusCode());
    }

    // ── helpers ────────────────────────────────────────────────

    private HttpResponse<String> post(String path, String body, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) b.header("Authorization", "Bearer " + token);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path)).GET();
        if (token != null) b.header("Authorization", "Bearer " + token);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }
}
