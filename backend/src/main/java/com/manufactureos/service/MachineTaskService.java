package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.dto.machineTask.MachineTaskResponse;
import com.manufactureos.entity.Machine;
import com.manufactureos.entity.MachineStatus;
import com.manufactureos.entity.MachineTask;
import com.manufactureos.entity.TaskStatus;
import com.manufactureos.exception.ConflictException;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.exception.SchedulingConflictException;
import com.manufactureos.repository.MachineMaintenanceRepository;
import com.manufactureos.repository.MachineRepository;
import com.manufactureos.repository.MachineTaskRepository;
import com.manufactureos.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Schedules machine tasks against a production order and drives a machine task's own
 * small lifecycle (SCHEDULED -> READY -> RUNNING -> PAUSED -> COMPLETED/CANCELLED),
 * keeping Machine.status in lockstep so no other task or maintenance window can be
 * booked against a machine that is already busy or down.
 *
 * Conflict prevention has two layers: an application-level overlap check here (so a
 * caller gets a clean 409 instead of a raw constraint violation), backed by the
 * database's own GIST exclusion constraint on machine_tasks as the final guarantee
 * under concurrent requests.
 */
@Service
public class MachineTaskService {

    private static final Set<TaskStatus> ACTIVE_TASK_STATUSES =
            EnumSet.of(TaskStatus.SCHEDULED, TaskStatus.READY, TaskStatus.RUNNING, TaskStatus.PAUSED);

    private final MachineTaskRepository machineTaskRepository;
    private final MachineRepository machineRepository;
    private final MachineMaintenanceRepository machineMaintenanceRepository;
    private final AuditService auditService;

    public MachineTaskService(MachineTaskRepository machineTaskRepository,
                               MachineRepository machineRepository,
                               MachineMaintenanceRepository machineMaintenanceRepository,
                               AuditService auditService) {
        this.machineTaskRepository = machineTaskRepository;
        this.machineRepository = machineRepository;
        this.machineMaintenanceRepository = machineMaintenanceRepository;
        this.auditService = auditService;
    }

    public List<MachineTaskResponse> listForMachine(Long machineId) {
        return machineTaskRepository.findByMachineId(machineId).stream().map(this::toResponse).toList();
    }

    public List<MachineTaskResponse> listForProductionOrder(Long productionOrderId) {
        return machineTaskRepository.findByProductionOrderId(productionOrderId).stream().map(this::toResponse).toList();
    }

    public MachineTaskResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public MachineTaskResponse schedule(Long machineId, Long productionOrderId, Instant startTime, Instant endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new ConflictException("INVALID_TIME_RANGE", "Task start time must be before end time");
        }

        // Lock the machine row so two concurrent schedule requests against the same
        // machine serialize here rather than both passing the overlap check.
        Machine machine = machineRepository.lockById(machineId)
                .orElseThrow(() -> new NotFoundException("Machine not found: " + machineId));

        if (machine.getStatus() == MachineStatus.MAINTENANCE
                || machine.getStatus() == MachineStatus.BROKEN
                || machine.getStatus() == MachineStatus.INACTIVE) {
            throw new ConflictException("MACHINE_UNAVAILABLE", "Machine " + machine.getCode()
                    + " is not available for scheduling (status " + machine.getStatus() + ")");
        }

        List<MachineTask> overlapping = machineTaskRepository.findOverlapping(machineId, startTime, endTime, null);
        if (!overlapping.isEmpty()) {
            throw new SchedulingConflictException("Machine " + machine.getCode()
                    + " already has a task scheduled in that window");
        }

        List<com.manufactureos.entity.MachineMaintenance> maintenanceOverlap =
                machineMaintenanceRepository.findOverlapping(machineId, startTime, endTime);
        if (!maintenanceOverlap.isEmpty()) {
            throw new SchedulingConflictException("Machine " + machine.getCode()
                    + " has maintenance scheduled in that window");
        }

        MachineTask task = new MachineTask();
        task.setMachineId(machineId);
        task.setProductionOrderId(productionOrderId);
        task.setStartTime(startTime);
        task.setEndTime(endTime);
        task.setStatus(TaskStatus.SCHEDULED);
        task.setCreatedBy(SecurityUtils.currentUserId().orElse(null));
        task.setCreatedAt(Instant.now());
        task = machineTaskRepository.save(task);

        auditService.log("MACHINE_TASK_SCHEDULED", "MachineTask", String.valueOf(task.getId()),
                "Scheduled machine " + machine.getCode() + " for production order " + productionOrderId);
        return toResponse(task);
    }

    @Transactional
    public MachineTaskResponse reschedule(Long id, Instant startTime, Instant endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new ConflictException("INVALID_TIME_RANGE", "Task start time must be before end time");
        }
        MachineTask task = find(id);
        requireStatus(task, EnumSet.of(TaskStatus.SCHEDULED, TaskStatus.READY));

        Machine machine = machineRepository.lockById(task.getMachineId())
                .orElseThrow(() -> new NotFoundException("Machine not found: " + task.getMachineId()));

        List<MachineTask> overlapping =
                machineTaskRepository.findOverlapping(machine.getId(), startTime, endTime, task.getId());
        if (!overlapping.isEmpty()) {
            throw new SchedulingConflictException("Machine " + machine.getCode()
                    + " already has a task scheduled in that window");
        }
        List<com.manufactureos.entity.MachineMaintenance> maintenanceOverlap =
                machineMaintenanceRepository.findOverlapping(machine.getId(), startTime, endTime);
        if (!maintenanceOverlap.isEmpty()) {
            throw new SchedulingConflictException("Machine " + machine.getCode()
                    + " has maintenance scheduled in that window");
        }

        task.setStartTime(startTime);
        task.setEndTime(endTime);
        auditService.log("MACHINE_TASK_RESCHEDULED", "MachineTask", String.valueOf(id), "Rescheduled task");
        return toResponse(task);
    }

    @Transactional
    public MachineTaskResponse markReady(Long id) {
        MachineTask task = find(id);
        requireStatus(task, EnumSet.of(TaskStatus.SCHEDULED));
        task.setStatus(TaskStatus.READY);
        return toResponse(task);
    }

    @Transactional
    public MachineTaskResponse start(Long id) {
        MachineTask task = find(id);
        requireStatus(task, EnumSet.of(TaskStatus.SCHEDULED, TaskStatus.READY));

        Machine machine = machineRepository.lockById(task.getMachineId())
                .orElseThrow(() -> new NotFoundException("Machine not found: " + task.getMachineId()));
        if (machine.getStatus() != MachineStatus.AVAILABLE) {
            throw new ConflictException("MACHINE_UNAVAILABLE", "Machine " + machine.getCode() + " is not available (status "
                    + machine.getStatus() + ")");
        }

        task.setStatus(TaskStatus.RUNNING);
        task.setActualStart(Instant.now());
        machine.setStatus(MachineStatus.IN_USE);

        auditService.log("MACHINE_TASK_STARTED", "MachineTask", String.valueOf(id),
                "Started task on machine " + machine.getCode());
        return toResponse(task);
    }

    @Transactional
    public MachineTaskResponse pause(Long id) {
        MachineTask task = find(id);
        requireStatus(task, EnumSet.of(TaskStatus.RUNNING));
        task.setStatus(TaskStatus.PAUSED);
        releaseMachineIfIdle(task.getMachineId(), task.getId());
        return toResponse(task);
    }

    @Transactional
    public MachineTaskResponse resume(Long id) {
        MachineTask task = find(id);
        requireStatus(task, EnumSet.of(TaskStatus.PAUSED));

        Machine machine = machineRepository.lockById(task.getMachineId())
                .orElseThrow(() -> new NotFoundException("Machine not found: " + task.getMachineId()));
        if (machine.getStatus() != MachineStatus.AVAILABLE) {
            throw new ConflictException("MACHINE_UNAVAILABLE", "Machine " + machine.getCode() + " is not available (status "
                    + machine.getStatus() + ")");
        }
        task.setStatus(TaskStatus.RUNNING);
        machine.setStatus(MachineStatus.IN_USE);
        return toResponse(task);
    }

    @Transactional
    public MachineTaskResponse complete(Long id) {
        MachineTask task = find(id);
        requireStatus(task, EnumSet.of(TaskStatus.RUNNING, TaskStatus.PAUSED));
        task.setStatus(TaskStatus.COMPLETED);
        task.setActualEnd(Instant.now());
        releaseMachineIfIdle(task.getMachineId(), task.getId());

        auditService.log("MACHINE_TASK_COMPLETED", "MachineTask", String.valueOf(id), "Completed machine task");
        return toResponse(task);
    }

    @Transactional
    public MachineTaskResponse cancel(Long id) {
        MachineTask task = find(id);
        requireStatus(task, EnumSet.of(TaskStatus.SCHEDULED, TaskStatus.READY, TaskStatus.RUNNING, TaskStatus.PAUSED));
        task.setStatus(TaskStatus.CANCELLED);
        releaseMachineIfIdle(task.getMachineId(), task.getId());

        auditService.log("MACHINE_TASK_CANCELLED", "MachineTask", String.valueOf(id), "Cancelled machine task");
        return toResponse(task);
    }

    /** Flips the machine back to AVAILABLE once it has no other RUNNING task. */
    private void releaseMachineIfIdle(Long machineId, Long excludeTaskId) {
        Machine machine = machineRepository.lockById(machineId)
                .orElseThrow(() -> new NotFoundException("Machine not found: " + machineId));
        if (machine.getStatus() != MachineStatus.IN_USE) {
            return;
        }
        boolean stillRunning = machineTaskRepository.findByMachineId(machineId).stream()
                .anyMatch(t -> !t.getId().equals(excludeTaskId) && t.getStatus() == TaskStatus.RUNNING);
        if (!stillRunning) {
            machine.setStatus(MachineStatus.AVAILABLE);
        }
    }

    private void requireStatus(MachineTask task, Set<TaskStatus> allowed) {
        if (!allowed.contains(task.getStatus())) {
            throw new com.manufactureos.exception.InvalidStateTransitionException(
                    "Machine task " + task.getId() + " is " + task.getStatus() + "; expected one of " + allowed);
        }
    }

    private MachineTask find(Long id) {
        return machineTaskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Machine task not found: " + id));
    }

    private MachineTaskResponse toResponse(MachineTask t) {
        MachineTaskResponse r = new MachineTaskResponse();
        r.setId(t.getId());
        r.setMachineId(t.getMachineId());
        r.setProductionOrderId(t.getProductionOrderId());
        r.setStartTime(t.getStartTime());
        r.setEndTime(t.getEndTime());
        r.setStatus(t.getStatus());
        r.setActualStart(t.getActualStart());
        r.setActualEnd(t.getActualEnd());
        r.setCreatedBy(t.getCreatedBy());
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }
}
