package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.dto.reworkOrder.ReworkOrderResponse;
import com.manufactureos.dto.qualityOps.ReworkCompleteRequest;
import com.manufactureos.dto.qualityOps.ReworkRequest;
import com.manufactureos.entity.InventoryCategory;
import com.manufactureos.entity.OutputStatus;
import com.manufactureos.entity.ProductionOutput;
import com.manufactureos.entity.ReworkOrder;
import com.manufactureos.entity.ReworkStatus;
import com.manufactureos.exception.BadRequestException;
import com.manufactureos.exception.InvalidStateTransitionException;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.repository.InventoryRepository;
import com.manufactureos.repository.ProductionOutputRepository;
import com.manufactureos.repository.ReworkOrderRepository;
import com.manufactureos.security.SecurityUtils;
import com.manufactureos.util.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Reworks the rejected portion of a production output rather than scrapping it
 * outright. A rework order is capped at the output's current pendingDispositionQuantity
 * (the quantity QualityInspectionService left QUARANTINED); completing it moves
 * whatever passes rework back to FINISHED_GOOD and reduces the pending amount, leaving
 * any remainder quarantined for ScrapService (or a further rework attempt) to resolve.
 */
@Service
public class ReworkService {

    private final ReworkOrderRepository reworkOrderRepository;
    private final ProductionOutputRepository productionOutputRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final AuditService auditService;

    public ReworkService(ReworkOrderRepository reworkOrderRepository,
                          ProductionOutputRepository productionOutputRepository,
                          InventoryRepository inventoryRepository,
                          InventoryService inventoryService,
                          AuditService auditService) {
        this.reworkOrderRepository = reworkOrderRepository;
        this.productionOutputRepository = productionOutputRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryService = inventoryService;
        this.auditService = auditService;
    }

    public List<ReworkOrderResponse> listForProductionOrder(Long productionOrderId) {
        return reworkOrderRepository.findByProductionOrderId(productionOrderId).stream()
                .map(this::toResponse).toList();
    }

    public ReworkOrderResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public ReworkOrderResponse request(ReworkRequest req) {
        ProductionOutput output = productionOutputRepository.lockById(req.getOutputId())
                .orElseThrow(() -> new NotFoundException("Production output not found: " + req.getOutputId()));

        if (req.getQuantity().compareTo(output.getPendingDispositionQuantity()) > 0) {
            throw new BadRequestException("INVALID_QUANTITY", "Rework quantity (" + req.getQuantity()
                    + ") exceeds the output's pending-disposition quantity (" + output.getPendingDispositionQuantity() + ")");
        }

        ReworkOrder rework = new ReworkOrder();
        rework.setReworkNumber(CodeGenerator.next("RWK"));
        rework.setOutputId(output.getId());
        rework.setProductionOrderId(output.getProductionOrderId());
        rework.setQuantity(req.getQuantity());
        rework.setPassedQuantity(BigDecimal.ZERO);
        rework.setStatus(ReworkStatus.REQUESTED);
        rework.setAssignedWorkerId(req.getAssignedWorkerId());
        rework.setMachineId(req.getMachineId());
        rework.setNotes(req.getNotes());
        rework.setAdditionalMaterials(req.getAdditionalMaterials());
        rework.setCreatedBy(SecurityUtils.currentUserId().orElse(null));
        rework.setCreatedAt(Instant.now());
        rework = reworkOrderRepository.save(rework);

        auditService.log("REWORK_REQUESTED", "ReworkOrder", String.valueOf(rework.getId()),
                "Requested rework of " + req.getQuantity() + " units from output " + output.getId());
        return toResponse(rework);
    }

    @Transactional
    public ReworkOrderResponse start(Long id) {
        ReworkOrder rework = find(id);
        requireStatus(rework, EnumSet.of(ReworkStatus.REQUESTED));
        rework.setStatus(ReworkStatus.IN_PROGRESS);
        return toResponse(rework);
    }

    @Transactional
    public ReworkOrderResponse complete(Long id, ReworkCompleteRequest req) {
        ReworkOrder rework = find(id);
        requireStatus(rework, EnumSet.of(ReworkStatus.IN_PROGRESS));

        BigDecimal passed = req.getPassedQuantity() != null ? req.getPassedQuantity() : BigDecimal.ZERO;
        if (passed.signum() < 0 || passed.compareTo(rework.getQuantity()) > 0) {
            throw new BadRequestException("INVALID_QUANTITY",
                    "Passed quantity must be between 0 and the rework order's quantity (" + rework.getQuantity() + ")");
        }

        ProductionOutput output = productionOutputRepository.lockById(rework.getOutputId())
                .orElseThrow(() -> new NotFoundException("Production output not found: " + rework.getOutputId()));

        if (passed.signum() > 0) {
            com.manufactureos.entity.Inventory quarantined = inventoryRepository.findByBatchId(output.getBatchId())
                    .stream()
                    .filter(i -> i.getCategory() == InventoryCategory.QUARANTINED)
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("No quarantined stock found for batch " + output.getBatchId()));
            inventoryService.moveCategory(quarantined.getId(), InventoryCategory.FINISHED_GOOD, passed,
                    "ReworkOrder", rework.getId(), "Rework recovered " + passed + " units");
        }

        rework.setPassedQuantity(passed);
        rework.setStatus(ReworkStatus.COMPLETED);
        rework.setOutcome(req.getOutcome());

        // The reworked quantity (whether it passed or is still scrap-bound) leaves the
        // output's pending-disposition bucket; anything that didn't pass stays
        // QUARANTINED physically and remains available for ScrapService to dispose of.
        output.setPendingDispositionQuantity(output.getPendingDispositionQuantity().subtract(rework.getQuantity()));
        output.setReleasedQuantity(output.getReleasedQuantity().add(passed));
        if (output.getPendingDispositionQuantity().signum() <= 0) {
            output.setPendingDispositionQuantity(BigDecimal.ZERO);
            output.setStatus(output.getReleasedQuantity().signum() > 0 ? OutputStatus.ACCEPTED : OutputStatus.REJECTED);
        }

        auditService.log("REWORK_COMPLETED", "ReworkOrder", String.valueOf(id),
                "Completed rework: " + passed + " of " + rework.getQuantity() + " passed");
        return toResponse(rework);
    }

    @Transactional
    public ReworkOrderResponse cancel(Long id) {
        ReworkOrder rework = find(id);
        requireStatus(rework, EnumSet.of(ReworkStatus.REQUESTED, ReworkStatus.IN_PROGRESS));
        rework.setStatus(ReworkStatus.CANCELLED);
        auditService.log("REWORK_CANCELLED", "ReworkOrder", String.valueOf(id), "Cancelled rework order");
        return toResponse(rework);
    }

    private void requireStatus(ReworkOrder rework, Set<ReworkStatus> allowed) {
        if (!allowed.contains(rework.getStatus())) {
            throw new InvalidStateTransitionException(
                    "Rework order " + rework.getId() + " is " + rework.getStatus() + "; expected one of " + allowed);
        }
    }

    private ReworkOrder find(Long id) {
        return reworkOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rework order not found: " + id));
    }

    private ReworkOrderResponse toResponse(ReworkOrder r) {
        ReworkOrderResponse resp = new ReworkOrderResponse();
        resp.setId(r.getId());
        resp.setReworkNumber(r.getReworkNumber());
        resp.setOutputId(r.getOutputId());
        resp.setProductionOrderId(r.getProductionOrderId());
        resp.setQuantity(r.getQuantity());
        resp.setPassedQuantity(r.getPassedQuantity());
        resp.setStatus(r.getStatus());
        resp.setAssignedWorkerId(r.getAssignedWorkerId());
        resp.setMachineId(r.getMachineId());
        resp.setNotes(r.getNotes());
        resp.setOutcome(r.getOutcome());
        resp.setAdditionalMaterials(r.getAdditionalMaterials());
        resp.setCreatedBy(r.getCreatedBy());
        resp.setCreatedAt(r.getCreatedAt());
        return resp;
    }
}
