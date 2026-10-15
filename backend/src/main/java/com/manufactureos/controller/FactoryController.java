package com.manufactureos.controller;

import com.manufactureos.dto.factory.FactoryRequest;
import com.manufactureos.dto.factory.FactoryResponse;
import com.manufactureos.service.FactoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/factories")
public class FactoryController {

    private final FactoryService service;

    public FactoryController(FactoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<FactoryResponse> list(@RequestParam(required = false) String q) {
        return service.list(q);
    }

    @GetMapping("/{id}")
    public FactoryResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<FactoryResponse> create(@Valid @RequestBody FactoryRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public FactoryResponse update(@PathVariable Long id, @Valid @RequestBody FactoryRequest req) {
        return service.update(id, req);
    }
}
