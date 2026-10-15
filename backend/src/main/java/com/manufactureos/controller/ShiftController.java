package com.manufactureos.controller;

import com.manufactureos.dto.shift.ShiftRequest;
import com.manufactureos.dto.shift.ShiftResponse;
import com.manufactureos.service.ShiftService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService service;

    public ShiftController(ShiftService service) {
        this.service = service;
    }

    @GetMapping
    public List<ShiftResponse> list(@RequestParam(required = false) String q) {
        return service.list(q);
    }

    @GetMapping("/{id}")
    public ShiftResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'PRODUCTION_MANAGER')")
    public ResponseEntity<ShiftResponse> create(@Valid @RequestBody ShiftRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'PRODUCTION_MANAGER')")
    public ShiftResponse update(@PathVariable Long id, @Valid @RequestBody ShiftRequest req) {
        return service.update(id, req);
    }
}
