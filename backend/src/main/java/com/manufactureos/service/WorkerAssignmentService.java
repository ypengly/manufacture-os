package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.dto.workerAssignment.WorkerAssignmentResponse;
import com.manufactureos.entity.AssignmentStatus;
import com.manufactureos.entity.Machine;
import com.manufactureos.entity.MachineTask;
import com.manufactureos.entity.Worker;
import com.manufactureos.entity.WorkerAssignment;
import com.manufactureos.exception.ConflictException;
import com.manufactureos.exception.InvalidStateTransitionException;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.exception.SchedulingConflictException;
import com.manufactureos.repository.MachineRepository;
import com.manufactureos.repository.MachineTaskRepository;
import com.manufactureos.repository.WorkerAssignmentRepository;
import com.manufactureos.repository.WorkerRepository;
import com.manufactureos.repository.WorkerSkillRepository;
import com.manufactureos.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Assigns workers to machine tasks / production orders, checking that the worker isn't
 * already committed to an overlapping assignment and, when the assignment is for a
 * machine task, that the worker holds the machine's required skill (if any).
 * Reassignment always records the supervisor's reason, per spec.
 */
@Service
public class WorkerAssignmentService {

    private final WorkerAssignmentRepository assignmentRepository;
    private final WorkerRepository workerRepository;
    private final WorkerSkillRepository workerSkillRepository;
    private final MachineTaskRepository machineTaskRepository;
    private final MachineRepository machineRepository;
    private final AuditService auditService;

    public WorkerAssignmentService(WorkerAssignmentRepository assignmentRepository,
                                    WorkerRepository workerRepository,
                                    WorkerSkillRepository workerSkillRepository,
                                    MachineTaskRepository machineTaskRepository,
                                    MachineRepository machineRepository,
                                    AuditService auditService) {
        this.assignmentRepository = assignmentRepository;
        this.workerRepository = workerRepository;
        this.workerSkillRepository = workerSkillRepository;
        this.machineTaskRepository = machineTaskRepository;
        this.machineRepository = machineRepository;
        this.auditService = auditService;
    }

    public List<WorkerAssignmentResponse> listForWorker(Long workerId) {
        return assignmentRepository.findByWorkerId(workerId).stream().map(this::toResponse).toList();
    }

    public List<WorkerAssignmentResponse> listForMachineTask(Long machineTaskId) {
        return assignmentRepository.findByMachineTaskId(machineTaskId).stream().map(this::toResponse).toList();
    }

    public List<WorkerAssignmentResponse> listForProductionOrder(Long productionOrderId) {
        return assignmentRepository.findByProductionOrderId(productionOrderId).stream().map(this::toResponse).toList();
    }

    public WorkerAssignmentResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public WorkerAssignmentResponse assign(Long workerId, Long machineTaskId, Long productionOrderId,
                                            Instant startTime, Instant endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new ConflictException("INVALID_TIME_RANGE", "Assignment start time must be before end time");
        }

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new NotFoundException("Worker not found: " + workerId));

        if (machineTaskId != null) {
            checkSkillMatch(worker, machineTaskId);
        }

        List<WorkerAssignment> overlapping = assignmentRepository.findOverlapping(workerId, startTime, endTime);
        if (!overlapping.isEmpty()) {
            throw new SchedulingConflictException("Worker " + worker.getFullName()
                    + " already has an overlapping assignment in that window");
        }

        WorkerAssignment a = new WorkerAssignment();
        a.setWorkerId(workerId);
        a.setMachineTaskId(machineTaskId);
        a.setProductionOrderId(productionOrderId);
        a.setStartTime(startTime);
        a.setEndTime(endTime);
        a.setStatus(AssignmentStatus.ACTIVE);
        a.setCreatedBy(SecurityUtils.currentUserId().orElse(null));
        a.setCreatedAt(Instant.now());
        a = assignmentRepository.save(a);

        auditService.log("WORKER_ASSIGNED", "WorkerAssignment", String.valueOf(a.getId()),
                "Assigned worker " + worker.getFullName() + " to "
                        + (machineTaskId != null ? "machine task " + machineTaskId : "production order " + productionOrderId));
        return toResponse(a);
    }

    /** Supervisor reassignment: closes the current assignment and opens a new one for a different worker, recording why. */
    @Transactional
    public WorkerAssignmentResponse reassign(Long assignmentId, Long newWorkerId, String reason,
                                              Instant startTime, Instant endTime) {
        WorkerAssignment current = find(assignmentId);
        requireStatus(current, EnumSet.of(AssignmentStatus.ACTIVE));

        Instant newStart = startTime != null ? startTime : current.getStartTime();
        Instant newEnd = endTime != null ? endTime : current.getEndTime();

        Worker newWorker = workerRepository.findById(newWorkerId)
                .orElseThrow(() -> new NotFoundException("Worker not found: " + newWorkerId));

        if (current.getMachineTaskId() != null) {
            checkSkillMatch(newWorker, current.getMachineTaskId());
        }

        List<WorkerAssignment> overlapping = assignmentRepository.findOverlapping(newWorkerId, newStart, newEnd);
        if (!overlapping.isEmpty()) {
            throw new SchedulingConflictException("Worker " + newWorker.getFullName()
                    + " already has an overlapping assignment in that window");
        }

        current.setStatus(AssignmentStatus.REASSIGNED);
        current.setReason(reason);

        WorkerAssignment next = new WorkerAssignment();
        next.setWorkerId(newWorkerId);
        next.setMachineTaskId(current.getMachineTaskId());
        next.setProductionOrderId(current.getProductionOrderId());
        next.setStartTime(newStart);
        next.setEndTime(newEnd);
        next.setStatus(AssignmentStatus.ACTIVE);
        next.setReason(reason);
        next.setCreatedBy(SecurityUtils.currentUserId().orElse(null));
        next.setCreatedAt(Instant.now());
        next = assignmentRepository.save(next);

        auditService.log("WORKER_REASSIGNED", "WorkerAssignment", String.valueOf(assignmentId),
                "Reassigned from worker " + current.getWorkerId() + " to " + newWorker.getFullName()
                        + " - reason: " + reason);
        return toResponse(next);
    }

    @Transactional
    public WorkerAssignmentResponse complete(Long id) {
        WorkerAssignment a = find(id);
        requireStatus(a, EnumSet.of(AssignmentStatus.ACTIVE));
        a.setStatus(AssignmentStatus.COMPLETED);
        return toResponse(a);
    }

    @Transactional
    public WorkerAssignmentResponse cancel(Long id, String reason) {
        WorkerAssignment a = find(id);
        requireStatus(a, EnumSet.of(AssignmentStatus.ACTIVE));
        a.setStatus(AssignmentStatus.CANCELLED);
        if (reason != null) {
            a.setReason(reason);
        }
        auditService.log("WORKER_ASSIGNMENT_CANCELLED", "WorkerAssignment", String.valueOf(id),
                "Cancelled assignment" + (reason != null ? " - reason: " + reason : ""));
        return toResponse(a);
    }

    private void checkSkillMatch(Worker worker, Long machineTaskId) {
        MachineTask task = machineTaskRepository.findById(machineTaskId)
                .orElseThrow(() -> new NotFoundException("Machine task not found: " + machineTaskId));
        Machine machine = machineRepository.findById(task.getMachineId())
                .orElseThrow(() -> new NotFoundException("Machine not found: " + task.getMachineId()));
        String requiredSkill = machine.getRequiredSkill();
        if (requiredSkill == null || requiredSkill.isBlank()) {
            return;
        }
        boolean hasSkill = workerSkillRepository.existsByWorkerIdAndSkillIgnoreCase(worker.getId(), requiredSkill);
        if (!hasSkill) {
            throw new ConflictException("SKILL_MISMATCH", "Worker " + worker.getFullName()
                    + " does not hold the required skill '" + requiredSkill + "' for machine " + machine.getCode());
        }
    }

    private void requireStatus(WorkerAssignment a, Set<AssignmentStatus> allowed) {
        if (!allowed.contains(a.getStatus())) {
            throw new InvalidStateTransitionException(
                    "Worker assignment " + a.getId() + " is " + a.getStatus() + "; expected one of " + allowed);
        }
    }

    private WorkerAssignment find(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Worker assignment not found: " + id));
    }

    private WorkerAssignmentResponse toResponse(WorkerAssignment a) {
        WorkerAssignmentResponse r = new WorkerAssignmentResponse();
        r.setId(a.getId());
        r.setWorkerId(a.getWorkerId());
        r.setMachineTaskId(a.getMachineTaskId());
        r.setProductionOrderId(a.getProductionOrderId());
        r.setStartTime(a.getStartTime());
        r.setEndTime(a.getEndTime());
        r.setStatus(a.getStatus());
        r.setReason(a.getReason());
        r.setCreatedBy(a.getCreatedBy());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
