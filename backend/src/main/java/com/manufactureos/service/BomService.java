package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.dto.bom.BomComponentInput;
import com.manufactureos.dto.bom.BomCreateRequest;
import com.manufactureos.dto.bom.BomDetailResponse;
import com.manufactureos.dto.bomComponent.BomComponentResponse;
import com.manufactureos.entity.Bom;
import com.manufactureos.entity.BomComponent;
import com.manufactureos.entity.BomStatus;
import com.manufactureos.exception.InvalidStateTransitionException;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.repository.BomComponentRepository;
import com.manufactureos.repository.BomRepository;
import com.manufactureos.security.SecurityUtils;
import com.manufactureos.util.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Bills of Material. A product can have several BOM versions over time, but only an
 * ACTIVE one may back a production order (enforced in ProductionOrderService); DRAFT
 * and PENDING_APPROVAL versions are work in progress, and ARCHIVED ones are kept only
 * for history, since production orders keep referencing the exact bom_id they used.
 */
@Service
public class BomService {

    private final BomRepository bomRepository;
    private final BomComponentRepository bomComponentRepository;
    private final AuditService auditService;

    public BomService(BomRepository bomRepository, BomComponentRepository bomComponentRepository,
                       AuditService auditService) {
        this.bomRepository = bomRepository;
        this.bomComponentRepository = bomComponentRepository;
        this.auditService = auditService;
    }

    @Transactional
    public BomDetailResponse create(BomCreateRequest req) {
        int nextVersion = bomRepository.findMaxVersion(req.getProductId()) + 1;

        Bom bom = new Bom();
        bom.setBomNumber(CodeGenerator.next("BOM"));
        bom.setProductId(req.getProductId());
        bom.setVersion(nextVersion);
        bom.setStatus(BomStatus.DRAFT);
        bom.setEffectiveFrom(req.getEffectiveFrom());
        bom.setEffectiveTo(req.getEffectiveTo());
        bom.setCreatedBy(SecurityUtils.currentUserId().orElse(null));
        bom = bomRepository.save(bom);

        for (BomComponentInput input : req.getComponents()) {
            BomComponent component = new BomComponent();
            component.setBomId(bom.getId());
            component.setMaterialId(input.getMaterialId());
            component.setQuantity(input.getQuantity());
            component.setWastePercent(input.getWastePercent());
            bomComponentRepository.save(component);
        }

        auditService.log("BOM_CREATED", "Bom", String.valueOf(bom.getId()),
                "BOM " + bom.getBomNumber() + " (v" + nextVersion + ") created for product " + req.getProductId());
        return toDetail(bom);
    }

    @Transactional(readOnly = true)
    public BomDetailResponse get(Long id) {
        return toDetail(find(id));
    }

    @Transactional(readOnly = true)
    public List<BomDetailResponse> listForProduct(Long productId) {
        return bomRepository.findByProductId(productId).stream().map(this::toDetail).toList();
    }

    @Transactional
    public BomDetailResponse submitForApproval(Long id) {
        Bom bom = find(id);
        requireStatus(bom, BomStatus.DRAFT);
        bom.setStatus(BomStatus.PENDING_APPROVAL);
        bomRepository.save(bom);
        auditService.log("BOM_SUBMITTED", "Bom", String.valueOf(bom.getId()), "Submitted for approval");
        return toDetail(bom);
    }

    /**
     * Approving a BOM activates it and, because only one BOM should be the active one
     * for a product at a time, archives whatever BOM was previously active for the same
     * product. The database's exclusion constraint on (product_id, effective range)
     * WHERE status = 'ACTIVE' is the final backstop if this logic is ever bypassed.
     */
    @Transactional
    public BomDetailResponse approve(Long id) {
        Bom bom = find(id);
        requireStatus(bom, BomStatus.PENDING_APPROVAL);

        for (Bom other : bomRepository.findByProductIdAndStatus(bom.getProductId(), BomStatus.ACTIVE)) {
            other.setStatus(BomStatus.ARCHIVED);
            bomRepository.save(other);
        }

        bom.setStatus(BomStatus.ACTIVE);
        bom.setApprovedBy(SecurityUtils.currentUserId().orElse(null));
        bom.setApprovedAt(Instant.now());
        bomRepository.save(bom);
        auditService.log("BOM_APPROVED", "Bom", String.valueOf(bom.getId()), "BOM approved and activated");
        return toDetail(bom);
    }

    @Transactional
    public BomDetailResponse archive(Long id) {
        Bom bom = find(id);
        if (bom.getStatus() == BomStatus.ARCHIVED) {
            return toDetail(bom);
        }
        bom.setStatus(BomStatus.ARCHIVED);
        bomRepository.save(bom);
        auditService.log("BOM_ARCHIVED", "Bom", String.valueOf(bom.getId()), "BOM archived");
        return toDetail(bom);
    }

    private void requireStatus(Bom bom, BomStatus required) {
        if (bom.getStatus() != required) {
            throw new InvalidStateTransitionException(
                    "BOM " + bom.getBomNumber() + " is " + bom.getStatus() + ", expected " + required + ".");
        }
    }

    private Bom find(Long id) {
        return bomRepository.findById(id).orElseThrow(() -> new NotFoundException("BOM not found: " + id));
    }

    private BomDetailResponse toDetail(Bom bom) {
        BomDetailResponse r = new BomDetailResponse();
        r.setId(bom.getId());
        r.setBomNumber(bom.getBomNumber());
        r.setProductId(bom.getProductId());
        r.setVersion(bom.getVersion());
        r.setStatus(bom.getStatus());
        r.setEffectiveFrom(bom.getEffectiveFrom());
        r.setEffectiveTo(bom.getEffectiveTo());
        r.setCreatedBy(bom.getCreatedBy());
        r.setApprovedBy(bom.getApprovedBy());
        r.setApprovedAt(bom.getApprovedAt());
        r.setCreatedAt(bom.getCreatedAt());
        r.setComponents(bomComponentRepository.findByBomId(bom.getId()).stream().map(c -> {
            BomComponentResponse cr = new BomComponentResponse();
            cr.setId(c.getId());
            cr.setBomId(c.getBomId());
            cr.setMaterialId(c.getMaterialId());
            cr.setQuantity(c.getQuantity());
            cr.setWastePercent(c.getWastePercent());
            return cr;
        }).toList());
        return r;
    }
}
