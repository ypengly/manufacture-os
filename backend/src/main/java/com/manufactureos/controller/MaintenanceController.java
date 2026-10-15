package com.manufactureos.controller;

import com.manufactureos.dto.machineMaintenance.MachineMaintenanceResponse;
import com.manufactureos.dto.schedulingOps.CompleteMaintenanceRequest;
import com.manufactureos.dto.schedulingOps.ScheduleMaintenanceRequest;
import com.manufactureos.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
public class MaintenanceController {

    private final MaintenanceService service;

    public MaintenanceController(MaintenanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<MachineMaintenanceResponse> listForMachine(@RequestParam Long machineId) {
        return service.listForMachine(machineId);
    }

    @GetMapping("/{id}")
    public MachineMaintenanceResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public MachineMaintenanceResponse schedule(@Valid @RequestBody ScheduleMaintenanceRequest request) {
        return service.schedule(request.getMachineId(), request.getMaintenanceType(), request.getDescription(),
                request.getScheduledStart(), request.getScheduledEnd(), request.getTechnician());
    }

    @PostMapping("/{id}/start")
    public MachineMaintenanceResponse start(@PathVariable Long id) {
        return service.start(id);
    }

    @PostMapping("/{id}/complete")
    public MachineMaintenanceResponse complete(@PathVariable Long id, @RequestBody(required = false) CompleteMaintenanceRequest request) {
        CompleteMaintenanceRequest body = request != null ? request : new CompleteMaintenanceRequest();
        return service.complete(id, body.getNotes(), body.getCost());
    }

    @PostMapping("/{id}/cancel")
    public MachineMaintenanceResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }
}
