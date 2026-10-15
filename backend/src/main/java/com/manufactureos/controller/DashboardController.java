package com.manufactureos.controller;

import com.manufactureos.security.SecurityUtils;
import com.manufactureos.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> admin() {
        return service.adminSummary();
    }

    @GetMapping("/production-manager")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER')")
    public Map<String, Object> productionManager() {
        return service.productionManagerSummary();
    }

    @GetMapping("/supervisor")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'PRODUCTION_MANAGER')")
    public Map<String, Object> supervisor() {
        return service.supervisorSummary();
    }

    @GetMapping("/quality-inspector")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'QUALITY_INSPECTOR')")
    public Map<String, Object> qualityInspector() {
        return service.qualityInspectorSummary();
    }

    @GetMapping("/inventory-manager")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INVENTORY_MANAGER')")
    public Map<String, Object> inventoryManager() {
        return service.inventoryManagerSummary();
    }

    @GetMapping("/worker")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'WORKER', 'SUPERVISOR')")
    public Map<String, Object> worker() {
        return service.workerSummary(SecurityUtils.requireCurrentUserId());
    }
}
