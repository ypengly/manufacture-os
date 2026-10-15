package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.dto.user.CreateUserRequest;
import com.manufactureos.dto.user.UpdateUserRolesRequest;
import com.manufactureos.dto.user.UserResponse;
import com.manufactureos.entity.User;
import com.manufactureos.exception.ConflictException;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest req) {
        if (userRepository.existsByUsernameIgnoreCase(req.getUsername())) {
            throw new ConflictException("DUPLICATE_USERNAME", "This username is already taken.");
        }
        if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new ConflictException("DUPLICATE_EMAIL", "This email is already registered.");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRoles(req.getRoles());
        user.setActive(true);
        user = userRepository.save(user);
        auditService.log("USER_CREATED", "User", String.valueOf(user.getId()),
                "User " + user.getUsername() + " created by an administrator");
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateRoles(Long id, UpdateUserRolesRequest req) {
        User user = find(id);
        user.setRoles(req.getRoles());
        userRepository.save(user);
        auditService.log("USER_ROLES_UPDATED", "User", String.valueOf(user.getId()), "Roles updated");
        return toResponse(user);
    }

    @Transactional
    public UserResponse setActive(Long id, boolean active) {
        User user = find(id);
        user.setActive(active);
        userRepository.save(user);
        auditService.log(active ? "USER_ACTIVATED" : "USER_DEACTIVATED",
                "User", String.valueOf(user.getId()), "Account " + (active ? "activated" : "deactivated"));
        return toResponse(user);
    }

    private User find(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    private UserResponse toResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setEmail(user.getEmail());
        r.setFullName(user.getFullName());
        r.setActive(user.getActive());
        r.setRoles(user.getRoles());
        r.setLastLoginAt(user.getLastLoginAt());
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }
}
