package com.manufactureos.service;

import com.manufactureos.config.JwtProperties;
import com.manufactureos.entity.Role;
import com.manufactureos.entity.User;
import com.manufactureos.security.JwtService;
import com.manufactureos.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtProperties properties(String secret) {
        JwtProperties props = new JwtProperties();
        props.setSecret(secret);
        props.setAccessTtlMinutes(15);
        props.setRefreshTtlDays(7);
        return props;
    }

    private UserPrincipal principal() {
        User user = new User();
        user.setId(42L);
        user.setUsername("supervisor1");
        user.setPasswordHash("irrelevant-hash");
        user.setActive(true);
        user.setRoles(Set.of(Role.SUPERVISOR));
        return new UserPrincipal(user);
    }

    @Test
    void rejectsASecretShorterThan32Bytes() {
        assertThatThrownBy(() -> new JwtService(properties("too-short")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void generatesATokenThatCarriesTheUserIdAndRoles() {
        JwtService jwtService = new JwtService(properties("0123456789abcdef0123456789abcdef"));
        String token = jwtService.generateAccessToken(principal());

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("supervisor1");

        Claims claims = jwtService.parseClaims(token);
        assertThat(claims.get("uid", Long.class)).isEqualTo(42L);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        assertThat(roles).containsExactly("SUPERVISOR");
    }

    @Test
    void aTokenSignedWithADifferentSecretIsNotValid() {
        JwtService signer = new JwtService(properties("0123456789abcdef0123456789abcdef"));
        JwtService verifier = new JwtService(properties("ffffffffffffffffffffffffffffffff"));

        String token = signer.generateAccessToken(principal());

        assertThat(verifier.isValid(token)).isFalse();
    }
}
