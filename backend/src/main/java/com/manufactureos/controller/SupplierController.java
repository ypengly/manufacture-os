package com.manufactureos.controller;

import com.manufactureos.dto.supplier.SupplierRequest;
import com.manufactureos.dto.supplier.SupplierResponse;
import com.manufactureos.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER', 'PRODUCTION_MANAGER', 'ADMIN', 'INVENTORY_MANAGER')")
    public List<SupplierResponse> list(@RequestParam(required = false) String q) {
        return service.list(q);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER', 'PRODUCTION_MANAGER', 'ADMIN', 'INVENTORY_MANAGER')")
    public SupplierResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER')")
    public SupplierResponse update(@PathVariable Long id, @Valid @RequestBody SupplierRequest req) {
        return service.update(id, req);
    }
}
