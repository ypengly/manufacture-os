package com.manufactureos.security;

import com.manufactureos.entity.Role;
import com.manufactureos.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security principal backed by our User entity. Authorities are the bare role
 * names (ADMIN, SUPERVISOR, ...) with no "ROLE_" prefix, matching the
 * hasAnyAuthority("ADMIN", ...) checks used throughout the controllers.
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final boolean active;
    private final Set<Role> roles;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.active = Boolean.TRUE.equals(user.getActive());
        this.roles = user.getRoles();
    }

    public Long getId() { return id; }

    public Set<Role> getRoles() { return roles; }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return active; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}
