package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.dto.qualityDefect.QualityDefectResponse;
import com.manufactureos.dto.qualityOps.DefectRequest;
import com.manufactureos.dto.qualityOps.DefectStatusUpdateRequest;
import com.manufactureos.entity.QualityDefect;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.repository.QualityDefectRepository;
import com.manufactureos.security.SecurityUtils;
import com.manufactureos.util.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Tracks defects found during inspection through to corrective action and closure. */
@Service
public class DefectService {

    private final QualityDefectRepository qualityDefectRepository;
    private final AuditService auditService;

    public DefectService(QualityDefectRepository qualityDefectRepository, AuditService auditService) {
        this.qualityDefectRepository = qualityDefectRepository;
        this.auditService = auditService;
    }

    public List<QualityDefectResponse> listForProductionOrder(Long productionOrderId) {
        return qualityDefectRepository.findByProductionOrderId(productionOrderId).stream()
                .map(this::toResponse).toList();
    }

    public List<QualityDefectResponse> listByStatus(com.manufactureos.entity.DefectStatus status) {
        return qualityDefectRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    public QualityDefectResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public QualityDefectResponse report(DefectRequest req) {
        QualityDefect defect = new QualityDefect();
        defect.setDefectCode(CodeGenerator.next("DEF"));
        defect.setDefectType(req.getDefectType());
        defect.setDescription(req.getDescription());
        defect.setSeverity(req.getSeverity());
        defect.setProductionOrderId(req.getProductionOrderId());
        defect.setProductId(req.getProductId());
        defect.setBatchId(req.getBatchId());
        defect.setReportedBy(SecurityUtils.currentUserId().orElse(null));
        defect.setCreatedAt(Instant.now());
        defect.setStatus(com.manufactureos.entity.DefectStatus.OPEN);
        defect = qualityDefectRepository.save(defect);

        auditService.log("DEFECT_REPORTED", "QualityDefect", String.valueOf(defect.getId()),
                "Reported " + req.getSeverity() + " defect: " + req.getDefectType());
        return toResponse(defect);
    }

    @Transactional
    public QualityDefectResponse updateStatus(Long id, DefectStatusUpdateRequest req) {
        QualityDefect defect = find(id);
        defect.setStatus(req.getStatus());
        if (req.getCorrectiveAction() != null) {
            defect.setCorrectiveAction(req.getCorrectiveAction());
        }
        auditService.log("DEFECT_STATUS_UPDATED", "QualityDefect", String.valueOf(id),
                "Status -> " + req.getStatus());
        return toResponse(defect);
    }

    private QualityDefect find(Long id) {
        return qualityDefectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quality defect not found: " + id));
    }

    private QualityDefectResponse toResponse(QualityDefect d) {
        QualityDefectResponse r = new QualityDefectResponse();
        r.setId(d.getId());
        r.setDefectCode(d.getDefectCode());
        r.setDefectType(d.getDefectType());
        r.setDescription(d.getDescription());
        r.setSeverity(d.getSeverity());
        r.setProductionOrderId(d.getProductionOrderId());
        r.setProductId(d.getProductId());
        r.setBatchId(d.getBatchId());
        r.setReportedBy(d.getReportedBy());
        r.setCreatedAt(d.getCreatedAt());
        r.setCorrectiveAction(d.getCorrectiveAction());
        r.setStatus(d.getStatus());
        return r;
    }
}
