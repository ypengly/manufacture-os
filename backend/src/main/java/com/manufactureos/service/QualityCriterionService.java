package com.manufactureos.service;

import com.manufactureos.entity.QualityCriterion;
import com.manufactureos.dto.qualityCriterion.QualityCriterionRequest;
import com.manufactureos.dto.qualityCriterion.QualityCriterionResponse;
import com.manufactureos.mapper.QualityCriterionMapper;
import com.manufactureos.repository.QualityCriterionRepository;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QualityCriterionService {

    private final QualityCriterionRepository repository;
    private final QualityCriterionMapper mapper;
    private final AuditService auditService;

    public QualityCriterionService(QualityCriterionRepository repository, QualityCriterionMapper mapper, AuditService auditService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<QualityCriterionResponse> list(String q) {
        List<QualityCriterion> all = repository.findAll();
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
    public QualityCriterionResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public QualityCriterionResponse create(QualityCriterionRequest req) {
        QualityCriterion e = new QualityCriterion();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("QUALITY_CRITERION_CREATED", "QualityCriterion", String.valueOf(e.getId()), "QualityCriterion created");
        return mapper.toResponse(e);
    }

    @Transactional
    public QualityCriterionResponse update(Long id, QualityCriterionRequest req) {
        QualityCriterion e = find(id);
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("QUALITY_CRITERION_UPDATED", "QualityCriterion", String.valueOf(e.getId()), "QualityCriterion updated");
        return mapper.toResponse(e);
    }

    private QualityCriterion find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("QualityCriterion not found: " + id));
    }
}
