package com.manufactureos.security;

import com.manufactureos.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Issues and validates short-lived JWT access tokens. Refresh tokens are deliberately
 * NOT JWTs: they are opaque random strings whose hash is stored in refresh_tokens, so
 * a single one can be revoked server-side (JwtService has no revocation list).
 */
@Service
public class JwtService {

    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        String secret = properties.getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "manufactureos.jwt.secret (JWT_SECRET) must be set and at least 32 bytes long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        List<String> roles = principal.getRoles().stream().map(Enum::name).collect(Collectors.toList());
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim(CLAIM_UID, principal.getId())
                .claim(CLAIM_ROLES, roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.getAccessTtlMinutes() * 60)))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }
}
