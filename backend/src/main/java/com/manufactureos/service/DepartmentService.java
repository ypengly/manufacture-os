package com.manufactureos.service;

import com.manufactureos.entity.Department;
import com.manufactureos.dto.department.DepartmentRequest;
import com.manufactureos.dto.department.DepartmentResponse;
import com.manufactureos.mapper.DepartmentMapper;
import com.manufactureos.repository.DepartmentRepository;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository repository;
    private final DepartmentMapper mapper;
    private final AuditService auditService;

    public DepartmentService(DepartmentRepository repository, DepartmentMapper mapper, AuditService auditService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> list(String q) {
        List<Department> all = repository.findAll();
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
    public DepartmentResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest req) {
        Department e = new Department();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("DEPARTMENT_CREATED", "Department", String.valueOf(e.getId()), "Department created");
        return mapper.toResponse(e);
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest req) {
        Department e = find(id);
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("DEPARTMENT_UPDATED", "Department", String.valueOf(e.getId()), "Department updated");
        return mapper.toResponse(e);
    }

    private Department find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Department not found: " + id));
    }
}
