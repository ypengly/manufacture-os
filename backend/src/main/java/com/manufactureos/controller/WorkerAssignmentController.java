package com.manufactureos.controller;

import com.manufactureos.dto.schedulingOps.AssignWorkerRequest;
import com.manufactureos.dto.schedulingOps.ReassignWorkerRequest;
import com.manufactureos.dto.workerAssignment.WorkerAssignmentResponse;
import com.manufactureos.service.WorkerAssignmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/worker-assignments")
public class WorkerAssignmentController {

    private final WorkerAssignmentService service;

    public WorkerAssignmentController(WorkerAssignmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<WorkerAssignmentResponse> list(@RequestParam(required = false) Long workerId,
                                                @RequestParam(required = false) Long machineTaskId,
                                                @RequestParam(required = false) Long productionOrderId) {
        if (workerId != null) {
            return service.listForWorker(workerId);
        }
        if (machineTaskId != null) {
            return service.listForMachineTask(machineTaskId);
        }
        if (productionOrderId != null) {
            return service.listForProductionOrder(productionOrderId);
        }
        return List.of();
    }

    @GetMapping("/{id}")
    public WorkerAssignmentResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
    public WorkerAssignmentResponse assign(@Valid @RequestBody AssignWorkerRequest request) {
        return service.assign(request.getWorkerId(), request.getMachineTaskId(), request.getProductionOrderId(),
                request.getStartTime(), request.getEndTime());
    }

    @PostMapping("/{id}/reassign")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
    public WorkerAssignmentResponse reassign(@PathVariable Long id, @Valid @RequestBody ReassignWorkerRequest request) {
        return service.reassign(id, request.getNewWorkerId(), request.getReason(),
                request.getStartTime(), request.getEndTime());
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
    public WorkerAssignmentResponse complete(@PathVariable Long id) {
        return service.complete(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')")
    public WorkerAssignmentResponse cancel(@PathVariable Long id, @RequestBody(required = false) ReasonBody body) {
        return service.cancel(id, body != null ? body.reason() : null);
    }

    public record ReasonBody(String reason) {}
}
