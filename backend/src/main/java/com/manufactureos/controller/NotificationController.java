package com.manufactureos.controller;

import com.manufactureos.dto.notification.NotificationResponse;
import com.manufactureos.entity.Role;
import com.manufactureos.security.SecurityUtils;
import com.manufactureos.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/mine")
    public Page<NotificationResponse> mine(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return service.listForUser(SecurityUtils.requireCurrentUserId(), PageRequest.of(page, size));
    }

    @GetMapping("/role/{role}")
    public Page<NotificationResponse> forRole(@PathVariable Role role,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return service.listForRole(role, PageRequest.of(page, size));
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id) {
        return service.markRead(id);
    }
}
