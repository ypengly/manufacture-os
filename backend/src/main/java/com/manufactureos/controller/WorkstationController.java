package com.manufactureos.controller;

import com.manufactureos.dto.workstation.WorkstationRequest;
import com.manufactureos.dto.workstation.WorkstationResponse;
import com.manufactureos.service.WorkstationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workstations")
public class WorkstationController {

    private final WorkstationService service;

    public WorkstationController(WorkstationService service) {
        this.service = service;
    }

    @GetMapping
    public List<WorkstationResponse> list(@RequestParam(required = false) String q) {
        return service.list(q);
    }

    @GetMapping("/{id}")
    public WorkstationResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<WorkstationResponse> create(@Valid @RequestBody WorkstationRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public WorkstationResponse update(@PathVariable Long id, @Valid @RequestBody WorkstationRequest req) {
        return service.update(id, req);
    }
}
