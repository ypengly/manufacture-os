package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.dto.qualityInspection.QualityInspectionResponse;
import com.manufactureos.dto.qualityOps.InspectionRequest;
import com.manufactureos.entity.BatchStatus;
import com.manufactureos.entity.Inventory;
import com.manufactureos.entity.InventoryBatch;
import com.manufactureos.entity.InventoryCategory;
import com.manufactureos.entity.InspectionResult;
import com.manufactureos.entity.OutputStatus;
import com.manufactureos.entity.ProductionOutput;
import com.manufactureos.entity.QualityInspection;
import com.manufactureos.exception.BadRequestException;
import com.manufactureos.exception.ConflictException;
import com.manufactureos.exception.InvalidStateTransitionException;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.repository.InventoryBatchRepository;
import com.manufactureos.repository.InventoryRepository;
import com.manufactureos.repository.ProductionOutputRepository;
import com.manufactureos.repository.QualityInspectionRepository;
import com.manufactureos.security.SecurityUtils;
import com.manufactureos.util.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Inspects quarantined production output and either releases it to FINISHED_GOOD stock
 * or leaves the failed portion in QUARANTINED for ReworkService / ScrapService to
 * dispose of. Finished goods are never released automatically: recording an inspection
 * only computes its result, and {@link #finalizeInspection} is the one place that
 * actually moves inventory - so a result can be reviewed (defects attached, etc.)
 * before it takes effect.
 */
@Service
public class QualityInspectionService {

    private final QualityInspectionRepository qualityInspectionRepository;
    private final ProductionOutputRepository productionOutputRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public QualityInspectionService(QualityInspectionRepository qualityInspectionRepository,
                                     ProductionOutputRepository productionOutputRepository,
                                     InventoryBatchRepository inventoryBatchRepository,
                                     InventoryRepository inventoryRepository,
                                     InventoryService inventoryService,
                                     NotificationService notificationService,
                                     AuditService auditService) {
        this.qualityInspectionRepository = qualityInspectionRepository;
        this.productionOutputRepository = productionOutputRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public List<QualityInspectionResponse> listForProductionOrder(Long productionOrderId) {
        return qualityInspectionRepository.findByProductionOrderId(productionOrderId).stream()
                .map(this::toResponse).toList();
    }

    public QualityInspectionResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public QualityInspectionResponse record(InspectionRequest req) {
        ProductionOutput output = productionOutputRepository.lockById(req.getOutputId())
                .orElseThrow(() -> new NotFoundException("Production output not found: " + req.getOutputId()));

        if (output.getStatus() != OutputStatus.PENDING_INSPECTION) {
            throw new InvalidStateTransitionException("Output " + output.getId()
                    + " is " + output.getStatus() + "; only PENDING_INSPECTION output can be inspected");
        }

        BigDecimal passed = req.getPassedQuantity() != null ? req.getPassedQuantity() : BigDecimal.ZERO;
        BigDecimal failed = req.getFailedQuantity() != null ? req.getFailedQuantity() : BigDecimal.ZERO;
        if (passed.signum() < 0 || failed.signum() < 0) {
            throw new BadRequestException("INVALID_QUANTITY", "Passed and failed quantities must not be negative");
        }
        BigDecimal total = passed.add(failed);
        if (total.compareTo(output.getQuantity()) > 0) {
            throw new BadRequestException("INVALID_QUANTITY",
                    "Passed + failed quantity (" + total + ") cannot exceed the produced quantity (" + output.getQuantity() + ")");
        }

        InspectionResult result;
        if (failed.signum() == 0 && passed.signum() > 0) {
            result = InspectionResult.PASSED;
        } else if (passed.signum() == 0 && failed.signum() > 0) {
            result = InspectionResult.FAILED;
        } else if (passed.signum() > 0) {
            result = InspectionResult.PARTIALLY_PASSED;
        } else {
            result = InspectionResult.PENDING;
        }

        QualityInspection inspection = new QualityInspection();
        inspection.setInspectionNumber(CodeGenerator.next("QI"));
        inspection.setProductionOrderId(output.getProductionOrderId());
        inspection.setProductId(output.getProductId());
        inspection.setOutputId(output.getId());
        inspection.setBatchId(output.getBatchId());
        inspection.setInspectorId(SecurityUtils.currentUserId().orElse(null));
        inspection.setInspectedAt(Instant.now());
        inspection.setSampleSize(req.getSampleSize());
        inspection.setPassedQuantity(passed);
        inspection.setFailedQuantity(failed);
        inspection.setResult(result);
        inspection.setMeasurements(req.getMeasurements());
        inspection.setNotes(req.getNotes());
        inspection.setFinalized(false);
        inspection.setRework(false);
        inspection = qualityInspectionRepository.save(inspection);

        auditService.log("QUALITY_INSPECTION_RECORDED", "QualityInspection", String.valueOf(inspection.getId()),
                "Inspected output " + output.getId() + ": result=" + result);
        return toResponse(inspection);
    }

    /** Applies the inspection's disposition: releases the passed portion to FINISHED_GOOD, leaves the failed
     *  portion QUARANTINED for rework/scrap, and updates the output and batch status accordingly. */
    @Transactional
    public QualityInspectionResponse finalizeInspection(Long inspectionId) {
        QualityInspection inspection = find(inspectionId);
        if (Boolean.TRUE.equals(inspection.getFinalized())) {
            throw new ConflictException("ALREADY_FINALIZED", "Inspection " + inspectionId + " has already been finalized");
        }

        ProductionOutput output = productionOutputRepository.lockById(inspection.getOutputId())
                .orElseThrow(() -> new NotFoundException("Production output not found: " + inspection.getOutputId()));

        InventoryBatch batch = inventoryBatchRepository.findById(inspection.getBatchId())
                .orElseThrow(() -> new NotFoundException("Batch not found: " + inspection.getBatchId()));

        Inventory quarantined = inventoryRepository.findByBatchId(batch.getId()).stream()
                .filter(i -> i.getCategory() == InventoryCategory.QUARANTINED)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No quarantined stock found for batch " + batch.getId()));

        BigDecimal passed = inspection.getPassedQuantity();
        BigDecimal failed = inspection.getFailedQuantity();

        if (passed.signum() > 0) {
            inventoryService.moveCategory(quarantined.getId(), InventoryCategory.FINISHED_GOOD, passed,
                    "QualityInspection", inspection.getId(), "QC accepted " + passed + " units");
        }
        // failed quantity is left in place (QUARANTINED) - ReworkService / ScrapService dispose of it next.

        output.setReleasedQuantity(output.getReleasedQuantity().add(passed));
        output.setRejectedQuantity(output.getRejectedQuantity().add(failed));
        output.setPendingDispositionQuantity(failed);
        if (failed.signum() == 0) {
            output.setStatus(OutputStatus.ACCEPTED);
        } else if (passed.signum() == 0) {
            output.setStatus(OutputStatus.REJECTED);
        } else {
            output.setStatus(OutputStatus.QUARANTINED);
        }

        batch.setStatus(failed.signum() == 0 ? BatchStatus.AVAILABLE : BatchStatus.QUARANTINED);

        inspection.setFinalized(true);

        if (failed.signum() > 0) {
            notificationService.notifyRole(com.manufactureos.entity.Role.PRODUCTION_MANAGER, "Batch failed inspection",
                    "Batch " + batch.getBatchNumber() + " had " + failed + " unit(s) fail inspection on output "
                            + output.getId() + " and needs rework or scrap disposition.",
                    com.manufactureos.entity.NotificationType.BATCH_FAILED, "BATCH_FAILED:" + inspectionId);
        }

        auditService.log("QUALITY_INSPECTION_FINALIZED", "QualityInspection", String.valueOf(inspectionId),
                "Finalized: released " + passed + ", left " + failed + " quarantined for disposition");
        return toResponse(inspection);
    }

    private QualityInspection find(Long id) {
        return qualityInspectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quality inspection not found: " + id));
    }

    private QualityInspectionResponse toResponse(QualityInspection i) {
        QualityInspectionResponse r = new QualityInspectionResponse();
        r.setId(i.getId());
        r.setInspectionNumber(i.getInspectionNumber());
        r.setProductionOrderId(i.getProductionOrderId());
        r.setProductId(i.getProductId());
        r.setOutputId(i.getOutputId());
        r.setBatchId(i.getBatchId());
        r.setInspectorId(i.getInspectorId());
        r.setInspectedAt(i.getInspectedAt());
        r.setSampleSize(i.getSampleSize());
        r.setPassedQuantity(i.getPassedQuantity());
        r.setFailedQuantity(i.getFailedQuantity());
        r.setResult(i.getResult());
        r.setMeasurements(i.getMeasurements());
        r.setNotes(i.getNotes());
        r.setFinalized(i.getFinalized());
        r.setRework(i.getRework());
        return r;
    }
}
