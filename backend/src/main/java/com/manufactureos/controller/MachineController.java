package com.manufactureos.controller;

import com.manufactureos.dto.machine.MachineRequest;
import com.manufactureos.dto.machine.MachineResponse;
import com.manufactureos.service.MachineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService service;

    public MachineController(MachineService service) {
        this.service = service;
    }

    @GetMapping
    public List<MachineResponse> list(@RequestParam(required = false) String q) {
        return service.list(q);
    }

    @GetMapping("/{id}")
    public MachineResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<MachineResponse> create(@Valid @RequestBody MachineRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public MachineResponse update(@PathVariable Long id, @Valid @RequestBody MachineRequest req) {
        return service.update(id, req);
    }
}
