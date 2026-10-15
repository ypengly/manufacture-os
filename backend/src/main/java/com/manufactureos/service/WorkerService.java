package com.manufactureos.service;

import com.manufactureos.entity.Worker;
import com.manufactureos.dto.worker.WorkerRequest;
import com.manufactureos.dto.worker.WorkerResponse;
import com.manufactureos.mapper.WorkerMapper;
import com.manufactureos.repository.WorkerRepository;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkerService {

    private final WorkerRepository repository;
    private final WorkerMapper mapper;
    private final AuditService auditService;

    public WorkerService(WorkerRepository repository, WorkerMapper mapper, AuditService auditService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<WorkerResponse> list(String q) {
        List<Worker> all = repository.findAll();
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            all = all.stream().filter(item -> {
                String q2 = needle;
                return (item.getEmployeeId() != null && item.getEmployeeId().toLowerCase().contains(q2)) || (item.getFullName() != null && item.getFullName().toLowerCase().contains(q2)) || (item.getJobTitle() != null && item.getJobTitle().toLowerCase().contains(q2));
            }).toList();
        }
        return all.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WorkerResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public WorkerResponse create(WorkerRequest req) {
        Worker e = new Worker();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("WORKER_CREATED", "Worker", String.valueOf(e.getId()), "Worker created");
        return mapper.toResponse(e);
    }

    @Transactional
    public WorkerResponse update(Long id, WorkerRequest req) {
        Worker e = find(id);
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("WORKER_UPDATED", "Worker", String.valueOf(e.getId()), "Worker updated");
        return mapper.toResponse(e);
    }

    private Worker find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Worker not found: " + id));
    }
}
