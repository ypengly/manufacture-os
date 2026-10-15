package com.manufactureos.controller;

import com.manufactureos.dto.qualityInspection.QualityInspectionResponse;
import com.manufactureos.dto.qualityOps.InspectionRequest;
import com.manufactureos.service.QualityInspectionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quality/inspections")
@PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
public class QualityInspectionController {

    private final QualityInspectionService service;

    public QualityInspectionController(QualityInspectionService service) {
        this.service = service;
    }

    @GetMapping
    public List<QualityInspectionResponse> list(@RequestParam Long productionOrderId) {
        return service.listForProductionOrder(productionOrderId);
    }

    @GetMapping("/{id}")
    public QualityInspectionResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR')")
    public QualityInspectionResponse record(@Valid @RequestBody InspectionRequest request) {
        return service.record(request);
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR')")
    public QualityInspectionResponse finalizeInspection(@PathVariable Long id) {
        return service.finalizeInspection(id);
    }
}
