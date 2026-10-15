package com.manufactureos.controller;

import com.manufactureos.dto.workerSkill.WorkerSkillResponse;
import com.manufactureos.service.WorkerSkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/worker-skills")
public class WorkerSkillController {

    private final WorkerSkillService service;

    public WorkerSkillController(WorkerSkillService service) {
        this.service = service;
    }

    @GetMapping
    public List<WorkerSkillResponse> listForWorker(@RequestParam Long workerId) {
        return service.listForWorker(workerId);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public WorkerSkillResponse add(@RequestParam Long workerId, @RequestParam String skill) {
        return service.add(workerId, skill);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Void> remove(@PathVariable Long id) {
        service.remove(id);
        return ResponseEntity.noContent().build();
    }
}
