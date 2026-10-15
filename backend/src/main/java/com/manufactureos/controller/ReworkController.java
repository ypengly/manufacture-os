package com.manufactureos.controller;

import com.manufactureos.dto.qualityOps.ReworkCompleteRequest;
import com.manufactureos.dto.qualityOps.ReworkRequest;
import com.manufactureos.dto.reworkOrder.ReworkOrderResponse;
import com.manufactureos.service.ReworkService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quality/rework")
@PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
public class ReworkController {

    private final ReworkService service;

    public ReworkController(ReworkService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReworkOrderResponse> list(@RequestParam Long productionOrderId) {
        return service.listForProductionOrder(productionOrderId);
    }

    @GetMapping("/{id}")
    public ReworkOrderResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR', 'PRODUCTION_MANAGER')")
    public ReworkOrderResponse request(@Valid @RequestBody ReworkRequest request) {
        return service.request(request);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'WORKER')")
    public ReworkOrderResponse start(@PathVariable Long id) {
        return service.start(id);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR', 'SUPERVISOR')")
    public ReworkOrderResponse complete(@PathVariable Long id, @Valid @RequestBody ReworkCompleteRequest request) {
        return service.complete(id, request);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public ReworkOrderResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }
}
