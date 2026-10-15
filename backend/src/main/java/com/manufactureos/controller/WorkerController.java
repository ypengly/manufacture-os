package com.manufactureos.controller;

import com.manufactureos.dto.worker.WorkerRequest;
import com.manufactureos.dto.worker.WorkerResponse;
import com.manufactureos.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerService service;

    public WorkerController(WorkerService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR', 'ADMIN', 'SUPERVISOR')")
    public List<WorkerResponse> list(@RequestParam(required = false) String q) {
        return service.list(q);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR', 'ADMIN', 'SUPERVISOR')")
    public WorkerResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<WorkerResponse> create(@Valid @RequestBody WorkerRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public WorkerResponse update(@PathVariable Long id, @Valid @RequestBody WorkerRequest req) {
        return service.update(id, req);
    }
}
