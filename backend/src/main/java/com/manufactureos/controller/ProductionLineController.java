package com.manufactureos.controller;

import com.manufactureos.dto.productionLine.ProductionLineRequest;
import com.manufactureos.dto.productionLine.ProductionLineResponse;
import com.manufactureos.service.ProductionLineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/production-lines")
public class ProductionLineController {

    private final ProductionLineService service;

    public ProductionLineController(ProductionLineService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductionLineResponse> list(@RequestParam(required = false) String q) {
        return service.list(q);
    }

    @GetMapping("/{id}")
    public ProductionLineResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ProductionLineResponse> create(@Valid @RequestBody ProductionLineRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ProductionLineResponse update(@PathVariable Long id, @Valid @RequestBody ProductionLineRequest req) {
        return service.update(id, req);
    }
}
