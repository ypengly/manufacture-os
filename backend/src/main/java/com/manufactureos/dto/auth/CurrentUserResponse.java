package com.manufactureos.dto.auth;

import com.manufactureos.entity.Role;

import java.util.Set;

public class CurrentUserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Set<Role> roles;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
