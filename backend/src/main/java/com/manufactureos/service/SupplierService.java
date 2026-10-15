package com.manufactureos.service;

import com.manufactureos.entity.Supplier;
import com.manufactureos.dto.supplier.SupplierRequest;
import com.manufactureos.dto.supplier.SupplierResponse;
import com.manufactureos.mapper.SupplierMapper;
import com.manufactureos.repository.SupplierRepository;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository repository;
    private final SupplierMapper mapper;
    private final AuditService auditService;

    public SupplierService(SupplierRepository repository, SupplierMapper mapper, AuditService auditService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> list(String q) {
        List<Supplier> all = repository.findAll();
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            all = all.stream().filter(item -> {
                String q2 = needle;
                return (item.getCode() != null && item.getCode().toLowerCase().contains(q2)) || (item.getCompanyName() != null && item.getCompanyName().toLowerCase().contains(q2)) || (item.getContactPerson() != null && item.getContactPerson().toLowerCase().contains(q2));
            }).toList();
        }
        return all.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest req) {
        Supplier e = new Supplier();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("SUPPLIER_CREATED", "Supplier", String.valueOf(e.getId()), "Supplier created");
        return mapper.toResponse(e);
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest req) {
        Supplier e = find(id);
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("SUPPLIER_UPDATED", "Supplier", String.valueOf(e.getId()), "Supplier updated");
        return mapper.toResponse(e);
    }

    private Supplier find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Supplier not found: " + id));
    }
}
