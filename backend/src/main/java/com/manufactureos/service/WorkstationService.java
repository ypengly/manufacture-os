package com.manufactureos.service;

import com.manufactureos.entity.Workstation;
import com.manufactureos.dto.workstation.WorkstationRequest;
import com.manufactureos.dto.workstation.WorkstationResponse;
import com.manufactureos.mapper.WorkstationMapper;
import com.manufactureos.repository.WorkstationRepository;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkstationService {

    private final WorkstationRepository repository;
    private final WorkstationMapper mapper;
    private final AuditService auditService;

    public WorkstationService(WorkstationRepository repository, WorkstationMapper mapper, AuditService auditService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<WorkstationResponse> list(String q) {
        List<Workstation> all = repository.findAll();
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            all = all.stream().filter(item -> {
                String q2 = needle;
                return (item.getName() != null && item.getName().toLowerCase().contains(q2));
            }).toList();
        }
        return all.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WorkstationResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public WorkstationResponse create(WorkstationRequest req) {
        Workstation e = new Workstation();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("WORKSTATION_CREATED", "Workstation", String.valueOf(e.getId()), "Workstation created");
        return mapper.toResponse(e);
    }

    @Transactional
    public WorkstationResponse update(Long id, WorkstationRequest req) {
        Workstation e = find(id);
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("WORKSTATION_UPDATED", "Workstation", String.valueOf(e.getId()), "Workstation updated");
        return mapper.toResponse(e);
    }

    private Workstation find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Workstation not found: " + id));
    }
}
