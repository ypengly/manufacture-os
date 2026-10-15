package com.manufactureos.service;

import com.manufactureos.entity.StorageLocation;
import com.manufactureos.dto.storageLocation.StorageLocationRequest;
import com.manufactureos.dto.storageLocation.StorageLocationResponse;
import com.manufactureos.mapper.StorageLocationMapper;
import com.manufactureos.repository.StorageLocationRepository;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StorageLocationService {

    private final StorageLocationRepository repository;
    private final StorageLocationMapper mapper;
    private final AuditService auditService;

    public StorageLocationService(StorageLocationRepository repository, StorageLocationMapper mapper, AuditService auditService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<StorageLocationResponse> list(String q) {
        List<StorageLocation> all = repository.findAll();
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
    public StorageLocationResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public StorageLocationResponse create(StorageLocationRequest req) {
        StorageLocation e = new StorageLocation();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("STORAGE_LOCATION_CREATED", "StorageLocation", String.valueOf(e.getId()), "StorageLocation created");
        return mapper.toResponse(e);
    }

    @Transactional
    public StorageLocationResponse update(Long id, StorageLocationRequest req) {
        StorageLocation e = find(id);
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("STORAGE_LOCATION_UPDATED", "StorageLocation", String.valueOf(e.getId()), "StorageLocation updated");
        return mapper.toResponse(e);
    }

    private StorageLocation find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("StorageLocation not found: " + id));
    }
}
