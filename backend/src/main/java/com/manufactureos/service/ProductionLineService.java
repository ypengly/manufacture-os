package com.manufactureos.service;

import com.manufactureos.entity.ProductionLine;
import com.manufactureos.dto.productionLine.ProductionLineRequest;
import com.manufactureos.dto.productionLine.ProductionLineResponse;
import com.manufactureos.mapper.ProductionLineMapper;
import com.manufactureos.repository.ProductionLineRepository;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductionLineService {

    private final ProductionLineRepository repository;
    private final ProductionLineMapper mapper;
    private final AuditService auditService;

    public ProductionLineService(ProductionLineRepository repository, ProductionLineMapper mapper, AuditService auditService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ProductionLineResponse> list(String q) {
        List<ProductionLine> all = repository.findAll();
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
    public ProductionLineResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public ProductionLineResponse create(ProductionLineRequest req) {
        ProductionLine e = new ProductionLine();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("PRODUCTION_LINE_CREATED", "ProductionLine", String.valueOf(e.getId()), "ProductionLine created");
        return mapper.toResponse(e);
    }

    @Transactional
    public ProductionLineResponse update(Long id, ProductionLineRequest req) {
        ProductionLine e = find(id);
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("PRODUCTION_LINE_UPDATED", "ProductionLine", String.valueOf(e.getId()), "ProductionLine updated");
        return mapper.toResponse(e);
    }

    private ProductionLine find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("ProductionLine not found: " + id));
    }
}
