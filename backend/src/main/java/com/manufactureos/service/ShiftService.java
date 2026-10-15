package com.manufactureos.service;

import com.manufactureos.entity.Shift;
import com.manufactureos.dto.shift.ShiftRequest;
import com.manufactureos.dto.shift.ShiftResponse;
import com.manufactureos.mapper.ShiftMapper;
import com.manufactureos.repository.ShiftRepository;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShiftService {

    private final ShiftRepository repository;
    private final ShiftMapper mapper;
    private final AuditService auditService;

    public ShiftService(ShiftRepository repository, ShiftMapper mapper, AuditService auditService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> list(String q) {
        List<Shift> all = repository.findAll();
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
    public ShiftResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public ShiftResponse create(ShiftRequest req) {
        Shift e = new Shift();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("SHIFT_CREATED", "Shift", String.valueOf(e.getId()), "Shift created");
        return mapper.toResponse(e);
    }

    @Transactional
    public ShiftResponse update(Long id, ShiftRequest req) {
        Shift e = find(id);
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("SHIFT_UPDATED", "Shift", String.valueOf(e.getId()), "Shift updated");
        return mapper.toResponse(e);
    }

    private Shift find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Shift not found: " + id));
    }
}
