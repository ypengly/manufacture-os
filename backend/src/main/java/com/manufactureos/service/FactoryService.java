package com.manufactureos.service;

import com.manufactureos.entity.Factory;
import com.manufactureos.dto.factory.FactoryRequest;
import com.manufactureos.dto.factory.FactoryResponse;
import com.manufactureos.mapper.FactoryMapper;
import com.manufactureos.repository.FactoryRepository;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FactoryService {

    private final FactoryRepository repository;
    private final FactoryMapper mapper;
    private final AuditService auditService;

    public FactoryService(FactoryRepository repository, FactoryMapper mapper, AuditService auditService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<FactoryResponse> list(String q) {
        List<Factory> all = repository.findAll();
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            all = all.stream().filter(item -> {
                String q2 = needle;
                return (item.getCode() != null && item.getCode().toLowerCase().contains(q2)) || (item.getName() != null && item.getName().toLowerCase().contains(q2)) || (item.getManagerName() != null && item.getManagerName().toLowerCase().contains(q2));
            }).toList();
        }
        return all.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FactoryResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public FactoryResponse create(FactoryRequest req) {
        Factory e = new Factory();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("FACTORY_CREATED", "Factory", String.valueOf(e.getId()), "Factory created");
        return mapper.toResponse(e);
    }

    @Transactional
    public FactoryResponse update(Long id, FactoryRequest req) {
        Factory e = find(id);
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("FACTORY_UPDATED", "Factory", String.valueOf(e.getId()), "Factory updated");
        return mapper.toResponse(e);
    }

    private Factory find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Factory not found: " + id));
    }
}
