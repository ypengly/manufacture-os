package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.config.JwtProperties;
import com.manufactureos.dto.auth.CurrentUserResponse;
import com.manufactureos.dto.auth.TokenPairResponse;
import com.manufactureos.entity.RefreshToken;
import com.manufactureos.entity.User;
import com.manufactureos.exception.BadRequestException;
import com.manufactureos.exception.ForbiddenException;
import com.manufactureos.repository.RefreshTokenRepository;
import com.manufactureos.repository.UserRepository;
import com.manufactureos.security.JwtService;
import com.manufactureos.security.TokenHasher;
import com.manufactureos.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AuthService(AuthenticationManager authenticationManager,
                        UserRepository userRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        JwtService jwtService,
                        JwtProperties jwtProperties,
                        PasswordEncoder passwordEncoder,
                        AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public TokenPairResponse login(String username, String password) {
        UserPrincipal principal;
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            principal = (UserPrincipal) authentication.getPrincipal();
        } catch (BadCredentialsException | org.springframework.security.authentication.DisabledException
                 | org.springframework.security.authentication.LockedException e) {
            throw new BadRequestException("INVALID_CREDENTIALS", "Invalid username or password.");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("INVALID_CREDENTIALS", "Invalid username or password."));
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        auditService.log("USER_LOGIN", "User", String.valueOf(user.getId()), "User logged in");
        return issueTokenPair(user);
    }

    @Transactional
    public TokenPairResponse refresh(String rawRefreshToken) {
        String hash = TokenHasher.sha256(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ForbiddenException("Refresh token is invalid or has been revoked."));

        if (Boolean.TRUE.equals(stored.getRevoked()) || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new ForbiddenException("Refresh token is invalid or has expired.");
        }

        // Rotate: revoke the presented token so it cannot be replayed, then issue a new pair.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ForbiddenException("Refresh token is invalid."));
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ForbiddenException("This account has been deactivated.");
        }
        return issueTokenPair(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = TokenHasher.sha256(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ForbiddenException("User not found."));
        return toCurrentUserResponse(user);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> maybeUser = userRepository.findByEmailIgnoreCase(email);
        // Always behave the same whether or not the email exists, so this endpoint
        // cannot be used to enumerate registered accounts.
        if (maybeUser.isEmpty()) {
            log.info("Password reset requested for unknown email");
            return;
        }
        User user = maybeUser.get();
        String rawToken = TokenHasher.generateOpaqueToken();
        user.setResetTokenHash(TokenHasher.sha256(rawToken));
        user.setResetTokenExpiresAt(Instant.now().plusSeconds(30 * 60));
        userRepository.save(user);

        // A real deployment sends rawToken by email via a transactional mail provider.
        // This project has no mail integration, so the token is only logged here.
        log.info("Password reset token for user {}: {}", user.getUsername(), rawToken);
    }

    @Transactional
    public void confirmPasswordReset(String rawToken, String newPassword) {
        String hash = TokenHasher.sha256(rawToken);
        User user = userRepository.findByResetTokenHash(hash)
                .orElseThrow(() -> new BadRequestException("INVALID_RESET_TOKEN", "This reset link is invalid or has expired."));
        if (user.getResetTokenExpiresAt() == null || user.getResetTokenExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("INVALID_RESET_TOKEN", "This reset link is invalid or has expired.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetTokenHash(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);
        auditService.log("PASSWORD_RESET", "User", String.valueOf(user.getId()), "Password reset via token");
    }

    private TokenPairResponse issueTokenPair(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);

        String rawRefreshToken = TokenHasher.generateOpaqueToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(TokenHasher.sha256(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshTtlDays() * 24 * 3600));
        refreshTokenRepository.save(refreshToken);

        return new TokenPairResponse(
                accessToken,
                rawRefreshToken,
                jwtProperties.getAccessTtlMinutes() * 60,
                toCurrentUserResponse(user));
    }

    private CurrentUserResponse toCurrentUserResponse(User user) {
        CurrentUserResponse response = new CurrentUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRoles(user.getRoles());
        return response;
    }
}
