package com.manufactureos.controller;

import com.manufactureos.dto.machineTask.MachineTaskResponse;
import com.manufactureos.dto.schedulingOps.RescheduleTaskRequest;
import com.manufactureos.dto.schedulingOps.ScheduleTaskRequest;
import com.manufactureos.service.MachineTaskService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machine-tasks")
public class MachineTaskController {

    private final MachineTaskService service;

    public MachineTaskController(MachineTaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<MachineTaskResponse> list(@RequestParam(required = false) Long machineId,
                                           @RequestParam(required = false) Long productionOrderId) {
        if (machineId != null) {
            return service.listForMachine(machineId);
        }
        if (productionOrderId != null) {
            return service.listForProductionOrder(productionOrderId);
        }
        return List.of();
    }

    @GetMapping("/{id}")
    public MachineTaskResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
    public MachineTaskResponse schedule(@Valid @RequestBody ScheduleTaskRequest request) {
        return service.schedule(request.getMachineId(), request.getProductionOrderId(),
                request.getStartTime(), request.getEndTime());
    }

    @PostMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
    public MachineTaskResponse reschedule(@PathVariable Long id, @Valid @RequestBody RescheduleTaskRequest request) {
        return service.reschedule(id, request.getStartTime(), request.getEndTime());
    }

    @PostMapping("/{id}/ready")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
    public MachineTaskResponse markReady(@PathVariable Long id) {
        return service.markReady(id);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'WORKER')")
    public MachineTaskResponse start(@PathVariable Long id) {
        return service.start(id);
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'WORKER')")
    public MachineTaskResponse pause(@PathVariable Long id) {
        return service.pause(id);
    }

    @PostMapping("/{id}/resume")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'WORKER')")
    public MachineTaskResponse resume(@PathVariable Long id) {
        return service.resume(id);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR', 'WORKER')")
    public MachineTaskResponse complete(@PathVariable Long id) {
        return service.complete(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
    public MachineTaskResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }
}
