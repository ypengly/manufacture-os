package com.manufactureos.controller;

import com.manufactureos.dto.qualityDefect.QualityDefectResponse;
import com.manufactureos.dto.qualityOps.DefectRequest;
import com.manufactureos.dto.qualityOps.DefectStatusUpdateRequest;
import com.manufactureos.entity.DefectStatus;
import com.manufactureos.service.DefectService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quality/defects")
@PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
public class DefectController {

    private final DefectService service;

    public DefectController(DefectService service) {
        this.service = service;
    }

    @GetMapping
    public List<QualityDefectResponse> list(@RequestParam(required = false) Long productionOrderId,
                                             @RequestParam(required = false) DefectStatus status) {
        if (productionOrderId != null) {
            return service.listForProductionOrder(productionOrderId);
        }
        if (status != null) {
            return service.listByStatus(status);
        }
        return List.of();
    }

    @GetMapping("/{id}")
    public QualityDefectResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR')")
    public QualityDefectResponse report(@Valid @RequestBody DefectRequest request) {
        return service.report(request);
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR')")
    public QualityDefectResponse updateStatus(@PathVariable Long id, @Valid @RequestBody DefectStatusUpdateRequest request) {
        return service.updateStatus(id, request);
    }
}
