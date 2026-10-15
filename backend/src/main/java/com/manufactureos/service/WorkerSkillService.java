package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.dto.workerSkill.WorkerSkillResponse;
import com.manufactureos.entity.Worker;
import com.manufactureos.entity.WorkerSkill;
import com.manufactureos.exception.ConflictException;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.repository.WorkerRepository;
import com.manufactureos.repository.WorkerSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkerSkillService {

    private final WorkerSkillRepository workerSkillRepository;
    private final WorkerRepository workerRepository;
    private final AuditService auditService;

    public WorkerSkillService(WorkerSkillRepository workerSkillRepository, WorkerRepository workerRepository,
                               AuditService auditService) {
        this.workerSkillRepository = workerSkillRepository;
        this.workerRepository = workerRepository;
        this.auditService = auditService;
    }

    public List<WorkerSkillResponse> listForWorker(Long workerId) {
        return workerSkillRepository.findByWorkerId(workerId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public WorkerSkillResponse add(Long workerId, String skill) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new NotFoundException("Worker not found: " + workerId));
        if (workerSkillRepository.existsByWorkerIdAndSkillIgnoreCase(workerId, skill)) {
            throw new ConflictException("DUPLICATE_SKILL", "Worker " + worker.getFullName()
                    + " already has the skill '" + skill + "' recorded");
        }
        WorkerSkill ws = new WorkerSkill();
        ws.setWorkerId(workerId);
        ws.setSkill(skill);
        ws = workerSkillRepository.save(ws);
        auditService.log("WORKER_SKILL_ADDED", "WorkerSkill", String.valueOf(ws.getId()),
                "Recorded skill '" + skill + "' for worker " + worker.getFullName());
        return toResponse(ws);
    }

    @Transactional
    public void remove(Long id) {
        WorkerSkill ws = workerSkillRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Worker skill not found: " + id));
        workerSkillRepository.delete(ws);
        auditService.log("WORKER_SKILL_REMOVED", "WorkerSkill", String.valueOf(id), "Removed worker skill");
    }

    private WorkerSkillResponse toResponse(WorkerSkill ws) {
        WorkerSkillResponse r = new WorkerSkillResponse();
        r.setId(ws.getId());
        r.setWorkerId(ws.getWorkerId());
        r.setSkill(ws.getSkill());
        return r;
    }
}
