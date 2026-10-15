package com.manufactureos.service;

import com.manufactureos.entity.Machine;
import com.manufactureos.entity.MachineStatus;
import com.manufactureos.entity.NotificationType;
import com.manufactureos.entity.Role;
import com.manufactureos.dto.machine.MachineRequest;
import com.manufactureos.dto.machine.MachineResponse;
import com.manufactureos.mapper.MachineMapper;
import com.manufactureos.repository.MachineRepository;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MachineService {

    private final MachineRepository repository;
    private final MachineMapper mapper;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public MachineService(MachineRepository repository, MachineMapper mapper, AuditService auditService,
                           NotificationService notificationService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<MachineResponse> list(String q) {
        List<Machine> all = repository.findAll();
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            all = all.stream().filter(item -> {
                String q2 = needle;
                return (item.getCode() != null && item.getCode().toLowerCase().contains(q2)) || (item.getName() != null && item.getName().toLowerCase().contains(q2)) || (item.getMachineType() != null && item.getMachineType().toLowerCase().contains(q2));
            }).toList();
        }
        return all.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MachineResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public MachineResponse create(MachineRequest req) {
        Machine e = new Machine();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("MACHINE_CREATED", "Machine", String.valueOf(e.getId()), "Machine created");
        return mapper.toResponse(e);
    }

    @Transactional
    public MachineResponse update(Long id, MachineRequest req) {
        Machine e = find(id);
        MachineStatus previousStatus = e.getStatus();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("MACHINE_UPDATED", "Machine", String.valueOf(e.getId()), "Machine updated");

        if (e.getStatus() == MachineStatus.BROKEN && previousStatus != MachineStatus.BROKEN) {
            notificationService.notifyRole(Role.PRODUCTION_MANAGER, "Machine failure: " + e.getCode(),
                    "Machine " + e.getCode() + " (" + e.getName() + ") was marked BROKEN and cannot be scheduled.",
                    NotificationType.MACHINE_FAILURE, null);
        }
        return mapper.toResponse(e);
    }

    private Machine find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Machine not found: " + id));
    }
}
