package com.manufactureos.controller;

import com.manufactureos.dto.storageLocation.StorageLocationRequest;
import com.manufactureos.dto.storageLocation.StorageLocationResponse;
import com.manufactureos.service.StorageLocationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/storage-locations")
public class StorageLocationController {

    private final StorageLocationService service;

    public StorageLocationController(StorageLocationService service) {
        this.service = service;
    }

    @GetMapping
    public List<StorageLocationResponse> list(@RequestParam(required = false) String q) {
        return service.list(q);
    }

    @GetMapping("/{id}")
    public StorageLocationResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<StorageLocationResponse> create(@Valid @RequestBody StorageLocationRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER')")
    public StorageLocationResponse update(@PathVariable Long id, @Valid @RequestBody StorageLocationRequest req) {
        return service.update(id, req);
    }
}
