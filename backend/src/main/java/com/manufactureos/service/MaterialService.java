package com.manufactureos.service;

import com.manufactureos.entity.Material;
import com.manufactureos.dto.material.MaterialRequest;
import com.manufactureos.dto.material.MaterialResponse;
import com.manufactureos.mapper.MaterialMapper;
import com.manufactureos.repository.MaterialRepository;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialService {

    private final MaterialRepository repository;
    private final MaterialMapper mapper;
    private final AuditService auditService;

    public MaterialService(MaterialRepository repository, MaterialMapper mapper, AuditService auditService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> list(String q) {
        List<Material> all = repository.findAll();
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            all = all.stream().filter(item -> {
                String q2 = needle;
                return (item.getCode() != null && item.getCode().toLowerCase().contains(q2)) || (item.getName() != null && item.getName().toLowerCase().contains(q2));
            }).toList();
        }
        return all.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MaterialResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public MaterialResponse create(MaterialRequest req) {
        Material e = new Material();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("MATERIAL_CREATED", "Material", String.valueOf(e.getId()), "Material created");
        return mapper.toResponse(e);
    }

    @Transactional
    public MaterialResponse update(Long id, MaterialRequest req) {
        Material e = find(id);
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("MATERIAL_UPDATED", "Material", String.valueOf(e.getId()), "Material updated");
        return mapper.toResponse(e);
    }

    private Material find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Material not found: " + id));
    }
}
