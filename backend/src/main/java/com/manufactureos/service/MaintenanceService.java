package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.dto.machineMaintenance.MachineMaintenanceResponse;
import com.manufactureos.entity.Machine;
import com.manufactureos.entity.MachineMaintenance;
import com.manufactureos.entity.MachineStatus;
import com.manufactureos.entity.MaintenanceStatus;
import com.manufactureos.entity.MaintenanceType;
import com.manufactureos.exception.ConflictException;
import com.manufactureos.exception.InvalidStateTransitionException;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.exception.SchedulingConflictException;
import com.manufactureos.repository.MachineMaintenanceRepository;
import com.manufactureos.repository.MachineRepository;
import com.manufactureos.repository.MachineTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Schedules and runs maintenance windows on machines. A SCHEDULED or IN_PROGRESS
 * maintenance window blocks any new machine-task from being booked against the same
 * machine in that window (see MachineTaskService); starting maintenance also flips the
 * machine itself to MAINTENANCE so no in-flight task can be started against it either.
 */
@Service
public class MaintenanceService {

    private final MachineMaintenanceRepository maintenanceRepository;
    private final MachineRepository machineRepository;
    private final MachineTaskRepository machineTaskRepository;
    private final AuditService auditService;

    public MaintenanceService(MachineMaintenanceRepository maintenanceRepository,
                               MachineRepository machineRepository,
                               MachineTaskRepository machineTaskRepository,
                               AuditService auditService) {
        this.maintenanceRepository = maintenanceRepository;
        this.machineRepository = machineRepository;
        this.machineTaskRepository = machineTaskRepository;
        this.auditService = auditService;
    }

    public List<MachineMaintenanceResponse> listForMachine(Long machineId) {
        return maintenanceRepository.findByMachineId(machineId).stream().map(this::toResponse).toList();
    }

    public MachineMaintenanceResponse get(Long id) {
        return toResponse(find(id));
    }

    /** Machines whose next scheduled maintenance date has arrived or passed. */
    public List<Machine> findMachinesDue() {
        return machineRepository.findByNextMaintenanceDateLessThanEqual(LocalDate.now());
    }

    @Transactional
    public MachineMaintenanceResponse schedule(Long machineId, MaintenanceType type, String description,
                                                Instant scheduledStart, Instant scheduledEnd, String technician) {
        if (!scheduledStart.isBefore(scheduledEnd)) {
            throw new ConflictException("INVALID_TIME_RANGE", "Scheduled start must be before scheduled end");
        }
        Machine machine = machineRepository.lockById(machineId)
                .orElseThrow(() -> new NotFoundException("Machine not found: " + machineId));

        List<MachineMaintenance> maintenanceOverlap =
                maintenanceRepository.findOverlapping(machineId, scheduledStart, scheduledEnd);
        if (!maintenanceOverlap.isEmpty()) {
            throw new SchedulingConflictException("Machine " + machine.getCode()
                    + " already has maintenance scheduled in that window");
        }
        List<com.manufactureos.entity.MachineTask> taskOverlap =
                machineTaskRepository.findOverlapping(machineId, scheduledStart, scheduledEnd, null);
        if (!taskOverlap.isEmpty()) {
            throw new SchedulingConflictException("Machine " + machine.getCode()
                    + " has a production task scheduled in that window");
        }

        MachineMaintenance m = new MachineMaintenance();
        m.setMachineId(machineId);
        m.setMaintenanceType(type);
        m.setDescription(description);
        m.setScheduledStart(scheduledStart);
        m.setScheduledEnd(scheduledEnd);
        m.setTechnician(technician);
        m.setStatus(MaintenanceStatus.SCHEDULED);
        m.setCreatedAt(Instant.now());
        m = maintenanceRepository.save(m);

        auditService.log("MAINTENANCE_SCHEDULED", "MachineMaintenance", String.valueOf(m.getId()),
                "Scheduled " + type + " maintenance for machine " + machine.getCode());
        return toResponse(m);
    }

    @Transactional
    public MachineMaintenanceResponse start(Long id) {
        MachineMaintenance m = find(id);
        requireStatus(m, EnumSet.of(MaintenanceStatus.SCHEDULED));

        Machine machine = machineRepository.lockById(m.getMachineId())
                .orElseThrow(() -> new NotFoundException("Machine not found: " + m.getMachineId()));
        if (machine.getStatus() == MachineStatus.IN_USE) {
            throw new ConflictException("MACHINE_IN_USE",
                    "Machine " + machine.getCode() + " is currently running a task and cannot enter maintenance");
        }

        m.setStatus(MaintenanceStatus.IN_PROGRESS);
        m.setStartedAt(Instant.now());
        machine.setStatus(MachineStatus.MAINTENANCE);

        auditService.log("MAINTENANCE_STARTED", "MachineMaintenance", String.valueOf(id),
                "Started maintenance on machine " + machine.getCode());
        return toResponse(m);
    }

    @Transactional
    public MachineMaintenanceResponse complete(Long id, String notes, BigDecimal cost) {
        MachineMaintenance m = find(id);
        requireStatus(m, EnumSet.of(MaintenanceStatus.IN_PROGRESS));

        Machine machine = machineRepository.lockById(m.getMachineId())
                .orElseThrow(() -> new NotFoundException("Machine not found: " + m.getMachineId()));

        m.setStatus(MaintenanceStatus.COMPLETED);
        m.setCompletedAt(Instant.now());
        if (notes != null) {
            m.setNotes(notes);
        }
        if (cost != null) {
            m.setCost(cost);
        }

        LocalDate today = LocalDate.now();
        machine.setStatus(MachineStatus.AVAILABLE);
        machine.setLastMaintenanceDate(today);
        if (machine.getMaintenanceIntervalDays() != null && machine.getMaintenanceIntervalDays() > 0) {
            machine.setNextMaintenanceDate(today.plusDays(machine.getMaintenanceIntervalDays()));
        }

        auditService.log("MAINTENANCE_COMPLETED", "MachineMaintenance", String.valueOf(id),
                "Completed maintenance on machine " + machine.getCode());
        return toResponse(m);
    }

    @Transactional
    public MachineMaintenanceResponse cancel(Long id) {
        MachineMaintenance m = find(id);
        requireStatus(m, EnumSet.of(MaintenanceStatus.SCHEDULED, MaintenanceStatus.IN_PROGRESS));

        if (m.getStatus() == MaintenanceStatus.IN_PROGRESS) {
            Machine machine = machineRepository.lockById(m.getMachineId())
                    .orElseThrow(() -> new NotFoundException("Machine not found: " + m.getMachineId()));
            machine.setStatus(MachineStatus.AVAILABLE);
        }
        m.setStatus(MaintenanceStatus.CANCELLED);

        auditService.log("MAINTENANCE_CANCELLED", "MachineMaintenance", String.valueOf(id), "Cancelled maintenance");
        return toResponse(m);
    }

    private void requireStatus(MachineMaintenance m, Set<MaintenanceStatus> allowed) {
        if (!allowed.contains(m.getStatus())) {
            throw new InvalidStateTransitionException(
                    "Maintenance " + m.getId() + " is " + m.getStatus() + "; expected one of " + allowed);
        }
    }

    private MachineMaintenance find(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Maintenance record not found: " + id));
    }

    private MachineMaintenanceResponse toResponse(MachineMaintenance m) {
        MachineMaintenanceResponse r = new MachineMaintenanceResponse();
        r.setId(m.getId());
        r.setMachineId(m.getMachineId());
        r.setMaintenanceType(m.getMaintenanceType());
        r.setDescription(m.getDescription());
        r.setScheduledStart(m.getScheduledStart());
        r.setScheduledEnd(m.getScheduledEnd());
        r.setStartedAt(m.getStartedAt());
        r.setCompletedAt(m.getCompletedAt());
        r.setTechnician(m.getTechnician());
        r.setCost(m.getCost());
        r.setStatus(m.getStatus());
        r.setNotes(m.getNotes());
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }
}
