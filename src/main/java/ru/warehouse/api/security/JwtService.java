package ru.warehouse.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long ttlMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
        byte[] bytes;
        try { bytes = Decoders.BASE64.decode(secret); }
        catch (Exception e) { bytes = secret.getBytes(StandardCharsets.UTF_8); }
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.ttlMs = ttlMinutes * 60_000L;
    }

    public record IssuedToken(String token, long expiresAtEpochMs) {}

    public IssuedToken issue(String subject, String role, String fullName) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(ttlMs);
        String token = Jwts.builder()
                .subject(subject)
                .claims(Map.of("role", role, "name", fullName))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
        return new IssuedToken(token, exp.toEpochMilli());
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }
}
