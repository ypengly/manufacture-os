package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.config.ScrapProperties;
import com.manufactureos.dto.qualityOps.ScrapRequest;
import com.manufactureos.dto.scrapRecord.ScrapRecordResponse;
import com.manufactureos.entity.Inventory;
import com.manufactureos.entity.InventoryCategory;
import com.manufactureos.entity.OutputStatus;
import com.manufactureos.entity.ProductionOutput;
import com.manufactureos.entity.ScrapRecord;
import com.manufactureos.exception.BadRequestException;
import com.manufactureos.exception.ForbiddenException;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.repository.InventoryRepository;
import com.manufactureos.repository.ProductionOutputRepository;
import com.manufactureos.repository.ScrapRecordRepository;
import com.manufactureos.security.SecurityUtils;
import com.manufactureos.util.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Permanently removes rejected stock from inventory as scrap. Any quantity at or above
 * {@link ScrapProperties#getApprovalThreshold()} requires the acting user to hold
 * ADMIN or PRODUCTION_MANAGER - a floor-level QUALITY_INSPECTOR can scrap small
 * quantities on their own authority but a large write-off needs sign-off, per spec.
 */
@Service
public class ScrapService {

    private final ScrapRecordRepository scrapRecordRepository;
    private final ProductionOutputRepository productionOutputRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final ScrapProperties scrapProperties;
    private final AuditService auditService;

    public ScrapService(ScrapRecordRepository scrapRecordRepository,
                         ProductionOutputRepository productionOutputRepository,
                         InventoryRepository inventoryRepository,
                         InventoryService inventoryService,
                         ScrapProperties scrapProperties,
                         AuditService auditService) {
        this.scrapRecordRepository = scrapRecordRepository;
        this.productionOutputRepository = productionOutputRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryService = inventoryService;
        this.scrapProperties = scrapProperties;
        this.auditService = auditService;
    }

    public List<ScrapRecordResponse> listForProductionOrder(Long productionOrderId) {
        return scrapRecordRepository.findByProductionOrderId(productionOrderId).stream()
                .map(this::toResponse).toList();
    }

    public ScrapRecordResponse get(Long id) {
        return toResponse(scrapRecordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Scrap record not found: " + id)));
    }

    @Transactional
    public ScrapRecordResponse scrap(ScrapRequest req) {
        if (req.getOutputId() == null && req.getInventoryId() == null) {
            throw new BadRequestException("MISSING_TARGET", "Either outputId or inventoryId must be provided");
        }

        boolean largeQuantity = req.getQuantity().compareTo(scrapProperties.getApprovalThreshold()) >= 0;
        if (largeQuantity && !SecurityUtils.currentUserHasAnyRole("ADMIN", "PRODUCTION_MANAGER")) {
            throw new ForbiddenException("Scrapping " + req.getQuantity()
                    + " units meets or exceeds the approval threshold (" + scrapProperties.getApprovalThreshold()
                    + ") and requires an ADMIN or PRODUCTION_MANAGER to authorize it");
        }

        Long inventoryId;
        Long batchId;
        ProductionOutput output = null;

        if (req.getOutputId() != null) {
            output = productionOutputRepository.lockById(req.getOutputId())
                    .orElseThrow(() -> new NotFoundException("Production output not found: " + req.getOutputId()));
            if (req.getQuantity().compareTo(output.getPendingDispositionQuantity()) > 0) {
                throw new BadRequestException("INVALID_QUANTITY", "Scrap quantity (" + req.getQuantity()
                        + ") exceeds the output's pending-disposition quantity (" + output.getPendingDispositionQuantity() + ")");
            }
            Inventory quarantined = inventoryRepository.findByBatchId(output.getBatchId()).stream()
                    .filter(i -> i.getCategory() == InventoryCategory.QUARANTINED)
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("No quarantined stock found for batch " + output.getBatchId()));
            inventoryId = quarantined.getId();
            batchId = output.getBatchId();
        } else {
            Inventory inventory = inventoryRepository.findById(req.getInventoryId())
                    .orElseThrow(() -> new NotFoundException("Inventory not found: " + req.getInventoryId()));
            inventoryId = inventory.getId();
            batchId = inventory.getBatchId();
        }

        ScrapRecord record = new ScrapRecord();
        record.setScrapNumber(CodeGenerator.next("SCR"));
        record.setProductionOrderId(req.getProductionOrderId());
        record.setInventoryId(inventoryId);
        record.setBatchId(batchId);
        record.setOutputId(req.getOutputId());
        record.setQuantity(req.getQuantity());
        record.setReason(req.getReason());
        record.setApprovedBy(largeQuantity ? SecurityUtils.currentUserId().orElse(null) : null);
        record.setCreatedBy(SecurityUtils.currentUserId().orElse(null));
        record.setCreatedAt(Instant.now());
        record = scrapRecordRepository.save(record);

        inventoryService.scrap(inventoryId, req.getQuantity(), "ScrapRecord", record.getId(), req.getReason());

        if (output != null) {
            BigDecimal quantity = req.getQuantity();
            output.setPendingDispositionQuantity(output.getPendingDispositionQuantity().subtract(quantity));
            if (output.getPendingDispositionQuantity().signum() <= 0) {
                output.setPendingDispositionQuantity(BigDecimal.ZERO);
                output.setStatus(output.getReleasedQuantity().signum() > 0 ? OutputStatus.ACCEPTED : OutputStatus.REJECTED);
            }
        }

        auditService.log("MATERIAL_SCRAPPED", "ScrapRecord", String.valueOf(record.getId()),
                "Scrapped " + req.getQuantity() + " units - reason: " + req.getReason());
        return toResponse(record);
    }

    private ScrapRecordResponse toResponse(ScrapRecord r) {
        ScrapRecordResponse resp = new ScrapRecordResponse();
        resp.setId(r.getId());
        resp.setScrapNumber(r.getScrapNumber());
        resp.setProductionOrderId(r.getProductionOrderId());
        resp.setInventoryId(r.getInventoryId());
        resp.setBatchId(r.getBatchId());
        resp.setOutputId(r.getOutputId());
        resp.setQuantity(r.getQuantity());
        resp.setReason(r.getReason());
        resp.setApprovedBy(r.getApprovedBy());
        resp.setCreatedBy(r.getCreatedBy());
        resp.setCreatedAt(r.getCreatedAt());
        return resp;
    }
}
