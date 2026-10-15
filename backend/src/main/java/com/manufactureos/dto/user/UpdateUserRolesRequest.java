package com.manufactureos.dto.user;

import com.manufactureos.entity.Role;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public class UpdateUserRolesRequest {

    @NotEmpty
    private Set<Role> roles;

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
