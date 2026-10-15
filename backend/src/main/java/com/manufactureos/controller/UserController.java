package com.manufactureos.controller;

import com.manufactureos.dto.user.CreateUserRequest;
import com.manufactureos.dto.user.UpdateUserRolesRequest;
import com.manufactureos.dto.user.UserResponse;
import com.manufactureos.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** User administration. Only ADMIN can manage accounts and roles, per the project spec. */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.list();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return userService.get(id);
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}/roles")
    public UserResponse updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
        return userService.updateRoles(id, request);
    }

    @PostMapping("/{id}/activate")
    public UserResponse activate(@PathVariable Long id) {
        return userService.setActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    public UserResponse deactivate(@PathVariable Long id) {
        return userService.setActive(id, false);
    }
}
