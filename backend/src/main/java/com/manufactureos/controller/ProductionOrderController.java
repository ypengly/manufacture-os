package com.manufactureos.controller;

import com.manufactureos.dto.inventoryOps.MaterialRequirementView;
import com.manufactureos.dto.productionOrderOps.*;
import com.manufactureos.entity.OrderStatus;
import com.manufactureos.service.ProductionOrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/production-orders")
public class ProductionOrderController {

    private final ProductionOrderService service;

    public ProductionOrderController(ProductionOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductionOrderDetailResponse> list(@RequestParam(required = false) OrderStatus status,
                                                      @RequestParam(required = false) Long factoryId,
                                                      @RequestParam(required = false) Long lineId) {
        return service.list(status, factoryId, lineId);
    }

    @GetMapping("/{id}")
    public ProductionOrderDetailResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/{id}/material-check")
    public List<MaterialRequirementView> checkMaterials(@PathVariable Long id) {
        return service.checkMaterials(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public ProductionOrderDetailResponse create(@Valid @RequestBody ProductionOrderCreateRequest request) {
        return service.create(request);
    }

    @PostMapping("/{id}/plan")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public ProductionOrderDetailResponse plan(@PathVariable Long id) {
        return service.plan(id);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public ProductionOrderDetailResponse submit(@PathVariable Long id) {
        return service.submitForApproval(id);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public ProductionOrderDetailResponse approve(@PathVariable Long id) {
        return service.approve(id);
    }

    @PostMapping("/{id}/reserve-materials")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'INVENTORY_MANAGER')")
    public ProductionOrderDetailResponse reserveMaterials(@PathVariable Long id,
                                                           @RequestParam(defaultValue = "false") boolean allowPartial) {
        return service.reserveMaterials(id, allowPartial);
    }

    @PostMapping("/{id}/ready")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public ProductionOrderDetailResponse markReady(@PathVariable Long id) {
        return service.markReady(id);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public ProductionOrderDetailResponse start(@PathVariable Long id) {
        return service.start(id);
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public ProductionOrderDetailResponse pause(@PathVariable Long id) {
        return service.pause(id);
    }

    @PostMapping("/{id}/resume")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public ProductionOrderDetailResponse resume(@PathVariable Long id) {
        return service.resume(id);
    }

    @PostMapping("/{id}/hold")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
    public ProductionOrderDetailResponse hold(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) {
        return service.hold(id, request.getReason());
    }

    @PostMapping("/{id}/resume-from-hold")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
    public ProductionOrderDetailResponse resumeFromHold(@PathVariable Long id) {
        return service.resumeFromHold(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public ProductionOrderDetailResponse cancel(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) {
        return service.cancel(id, request.getReason());
    }

    @PostMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public ProductionOrderDetailResponse reschedule(@PathVariable Long id, @Valid @RequestBody RescheduleRequest request) {
        return service.reschedule(id, request.getPlannedStart(), request.getPlannedEnd(), request.getOverrideReason());
    }

    @PostMapping("/{id}/report")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'WORKER')")
    public ProductionOrderDetailResponse report(@PathVariable Long id, @Valid @RequestBody ProductionReportRequest request) {
        return service.recordProduction(id, request);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public ProductionOrderDetailResponse complete(@PathVariable Long id) {
        return service.complete(id);
    }
}
