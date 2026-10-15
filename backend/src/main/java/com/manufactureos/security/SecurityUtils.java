package com.manufactureos.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    private static Optional<UserPrincipal> currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public static Optional<Long> currentUserId() {
        return currentPrincipal().map(UserPrincipal::getId);
    }

    public static Optional<String> currentUsername() {
        return currentPrincipal().map(UserPrincipal::getUsername);
    }

    public static Long requireCurrentUserId() {
        return currentUserId().orElseThrow(() -> new IllegalStateException("No authenticated user in context"));
    }

    /** True if the current principal holds at least one of the given bare role names (e.g. "ADMIN"). */
    public static boolean currentUserHasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        java.util.Set<String> held = auth.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());
        for (String role : roles) {
            if (held.contains(role)) {
                return true;
            }
        }
        return false;
    }
}
