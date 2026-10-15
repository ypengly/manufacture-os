package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.dto.inventoryOps.MaterialRequirementView;
import com.manufactureos.dto.productionOrderItem.ProductionOrderItemResponse;
import com.manufactureos.dto.productionOrderOps.ProductionOrderCreateRequest;
import com.manufactureos.dto.productionOrderOps.ProductionOrderDetailResponse;
import com.manufactureos.dto.productionOrderOps.ProductionReportRequest;
import com.manufactureos.entity.*;
import com.manufactureos.exception.BadRequestException;
import com.manufactureos.exception.InsufficientMaterialException;
import com.manufactureos.exception.InvalidStateTransitionException;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.repository.*;
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
 * Drives the production order lifecycle described in the project spec:
 * DRAFT -> PLANNED -> PENDING_APPROVAL -> APPROVED -> MATERIALS_RESERVED -> READY ->
 * IN_PROGRESS -> COMPLETED, with PAUSED/ON_HOLD/CANCELLED as side branches. Every
 * transition is validated here rather than trusted from the client, and material
 * reservation/consumption is always delegated to InventoryService so the locking and
 * transaction-logging rules there apply uniformly.
 */
@Service
public class ProductionOrderService {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionOrderItemRepository productionOrderItemRepository;
    private final ProductionReportRepository productionReportRepository;
    private final ProductionOutputRepository productionOutputRepository;
    private final BomRepository bomRepository;
    private final MrpService mrpService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public ProductionOrderService(ProductionOrderRepository productionOrderRepository,
                                   ProductionOrderItemRepository productionOrderItemRepository,
                                   ProductionReportRepository productionReportRepository,
                                   ProductionOutputRepository productionOutputRepository,
                                   BomRepository bomRepository,
                                   MrpService mrpService,
                                   InventoryService inventoryService,
                                   NotificationService notificationService,
                                   AuditService auditService) {
        this.productionOrderRepository = productionOrderRepository;
        this.productionOrderItemRepository = productionOrderItemRepository;
        this.productionReportRepository = productionReportRepository;
        this.productionOutputRepository = productionOutputRepository;
        this.bomRepository = bomRepository;
        this.mrpService = mrpService;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    // ---------------------------------------------------------------- create / read

    @Transactional
    public ProductionOrderDetailResponse create(ProductionOrderCreateRequest req) {
        if (!req.getPlannedEnd().isAfter(req.getPlannedStart())) {
            throw new BadRequestException("INVALID_SCHEDULE", "Planned end must be after planned start.");
        }

        Bom bom;
        if (req.getBomId() != null) {
            bom = bomRepository.findById(req.getBomId())
                    .orElseThrow(() -> new NotFoundException("BOM not found: " + req.getBomId()));
            if (!bom.getProductId().equals(req.getProductId())) {
                throw new BadRequestException("BOM_PRODUCT_MISMATCH", "That BOM does not belong to this product.");
            }
        } else {
            List<Bom> active = bomRepository.findByProductIdAndStatus(req.getProductId(), BomStatus.ACTIVE);
            if (active.isEmpty()) {
                throw new BadRequestException("NO_ACTIVE_BOM", "This product has no active BOM to produce against.");
            }
            bom = active.get(0);
        }
        if (bom.getStatus() != BomStatus.ACTIVE) {
            throw new InvalidStateTransitionException("Only an ACTIVE BOM can be used for production.");
        }

        ProductionOrder po = new ProductionOrder();
        po.setOrderNumber(CodeGenerator.next("PO"));
        po.setProductId(req.getProductId());
        po.setBomId(bom.getId());
        po.setPlannedQuantity(req.getPlannedQuantity());
        po.setFactoryId(req.getFactoryId());
        po.setLineId(req.getLineId());
        po.setPriority(req.getPriority());
        po.setPlannedStart(req.getPlannedStart());
        po.setPlannedEnd(req.getPlannedEnd());
        po.setStatus(OrderStatus.DRAFT);
        po.setCreatedBy(SecurityUtils.currentUserId().orElse(null));
        po = productionOrderRepository.save(po);

        List<MaterialRequirementView> requirements =
                mrpService.calculateRequirements(bom.getId(), req.getPlannedQuantity(), req.getFactoryId());
        for (MaterialRequirementView r : requirements) {
            ProductionOrderItem item = new ProductionOrderItem();
            item.setProductionOrderId(po.getId());
            item.setMaterialId(r.getMaterialId());
            item.setRequiredQuantity(r.getRequiredQuantity());
            productionOrderItemRepository.save(item);
        }

        auditService.log("PRODUCTION_ORDER_CREATED", "ProductionOrder", String.valueOf(po.getId()),
                "Order " + po.getOrderNumber() + " created for " + req.getPlannedQuantity() + " units");
        return toDetail(po);
    }

    @Transactional(readOnly = true)
    public ProductionOrderDetailResponse get(Long id) {
        return toDetail(find(id));
    }

    @Transactional(readOnly = true)
    public List<ProductionOrderDetailResponse> list(OrderStatus status, Long factoryId, Long lineId) {
        List<ProductionOrder> orders;
        if (status != null) orders = productionOrderRepository.findByStatus(status);
        else if (lineId != null) orders = productionOrderRepository.findByLineId(lineId);
        else if (factoryId != null) orders = productionOrderRepository.findByFactoryId(factoryId);
        else orders = productionOrderRepository.findAll();
        return orders.stream().map(this::toDetail).toList();
    }

    @Transactional(readOnly = true)
    public List<MaterialRequirementView> checkMaterials(Long id) {
        ProductionOrder po = find(id);
        return mrpService.calculateRequirements(po.getBomId(), po.getPlannedQuantity(), po.getFactoryId());
    }

    // ---------------------------------------------------------------- lifecycle

    @Transactional
    public ProductionOrderDetailResponse plan(Long id) {
        ProductionOrder po = requireStatus(find(id), EnumSet.of(OrderStatus.DRAFT));
        po.setStatus(OrderStatus.PLANNED);
        productionOrderRepository.save(po);
        return toDetail(po);
    }

    @Transactional
    public ProductionOrderDetailResponse submitForApproval(Long id) {
        ProductionOrder po = requireStatus(find(id), EnumSet.of(OrderStatus.PLANNED));
        po.setStatus(OrderStatus.PENDING_APPROVAL);
        productionOrderRepository.save(po);
        return toDetail(po);
    }

    @Transactional
    public ProductionOrderDetailResponse approve(Long id) {
        ProductionOrder po = requireStatus(find(id), EnumSet.of(OrderStatus.PENDING_APPROVAL));
        po.setStatus(OrderStatus.APPROVED);
        po.setApprovedBy(SecurityUtils.currentUserId().orElse(null));
        productionOrderRepository.save(po);
        auditService.log("PRODUCTION_ORDER_APPROVED", "ProductionOrder", String.valueOf(po.getId()), "Order approved");
        return toDetail(po);
    }

    /**
     * Reserves every required material for this order. If allowPartialMaterials is
     * false and any material falls short, the whole reservation attempt is rolled
     * back and the order stays APPROVED - exactly the "do not allow production to
     * start when required materials are unavailable" rule from the spec. Setting
     * allowPartialMaterials lets an authorized planner proceed anyway with whatever
     * could be reserved.
     */
    @Transactional
    public ProductionOrderDetailResponse reserveMaterials(Long id, boolean allowPartialMaterials) {
        ProductionOrder po = requireStatus(find(id), EnumSet.of(OrderStatus.APPROVED));
        po.setAllowPartialMaterials(allowPartialMaterials);

        for (ProductionOrderItem item : productionOrderItemRepository.findByProductionOrderId(po.getId())) {
            BigDecimal outstanding = item.getRequiredQuantity().subtract(item.getReservedQuantity());
            if (outstanding.signum() <= 0) continue;
            List<MaterialReservation> created = inventoryService.reserveMaterial(
                    po.getId(), item.getMaterialId(), outstanding, po.getFactoryId(), allowPartialMaterials);
            BigDecimal reserved = created.stream().map(MaterialReservation::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setReservedQuantity(item.getReservedQuantity().add(reserved));
            productionOrderItemRepository.save(item);
        }

        po.setStatus(OrderStatus.MATERIALS_RESERVED);
        productionOrderRepository.save(po);
        auditService.log("MATERIAL_RESERVED", "ProductionOrder", String.valueOf(po.getId()),
                "Materials reserved" + (allowPartialMaterials ? " (partial allowed)" : ""));
        return toDetail(po);
    }

    @Transactional
    public ProductionOrderDetailResponse markReady(Long id) {
        ProductionOrder po = requireStatus(find(id), EnumSet.of(OrderStatus.MATERIALS_RESERVED));
        if (!Boolean.TRUE.equals(po.getAllowPartialMaterials())) {
            for (ProductionOrderItem item : productionOrderItemRepository.findByProductionOrderId(po.getId())) {
                if (item.getReservedQuantity().compareTo(item.getRequiredQuantity()) < 0) {
                    throw new InsufficientMaterialException(
                            "Material " + item.getMaterialId() + " is not fully reserved for this order.");
                }
            }
        }
        po.setStatus(OrderStatus.READY);
        productionOrderRepository.save(po);
        return toDetail(po);
    }

    @Transactional
    public ProductionOrderDetailResponse start(Long id) {
        ProductionOrder po = requireStatus(find(id), EnumSet.of(OrderStatus.READY));
        po.setStatus(OrderStatus.IN_PROGRESS);
        po.setActualStart(Instant.now());
        productionOrderRepository.save(po);
        auditService.log("PRODUCTION_STARTED", "ProductionOrder", String.valueOf(po.getId()), "Production started");
        return toDetail(po);
    }

    @Transactional
    public ProductionOrderDetailResponse pause(Long id) {
        ProductionOrder po = requireStatus(find(id), EnumSet.of(OrderStatus.IN_PROGRESS));
        po.setStatus(OrderStatus.PAUSED);
        productionOrderRepository.save(po);
        return toDetail(po);
    }

    @Transactional
    public ProductionOrderDetailResponse resume(Long id) {
        ProductionOrder po = requireStatus(find(id), EnumSet.of(OrderStatus.PAUSED));
        po.setStatus(OrderStatus.IN_PROGRESS);
        productionOrderRepository.save(po);
        return toDetail(po);
    }

    @Transactional
    public ProductionOrderDetailResponse hold(Long id, String reason) {
        ProductionOrder po = requireStatus(find(id),
                EnumSet.of(OrderStatus.READY, OrderStatus.MATERIALS_RESERVED, OrderStatus.IN_PROGRESS, OrderStatus.PAUSED));
        po.setStatusBeforeHold(po.getStatus());
        po.setStatus(OrderStatus.ON_HOLD);
        po.setStatusReason(reason);
        productionOrderRepository.save(po);
        return toDetail(po);
    }

    @Transactional
    public ProductionOrderDetailResponse resumeFromHold(Long id) {
        ProductionOrder po = requireStatus(find(id), EnumSet.of(OrderStatus.ON_HOLD));
        if (po.getStatusBeforeHold() == null) {
            throw new InvalidStateTransitionException("This order has no prior status to resume to.");
        }
        po.setStatus(po.getStatusBeforeHold());
        po.setStatusBeforeHold(null);
        productionOrderRepository.save(po);
        return toDetail(po);
    }

    @Transactional
    public ProductionOrderDetailResponse cancel(Long id, String reason) {
        ProductionOrder po = find(id);
        if (po.getStatus() == OrderStatus.COMPLETED || po.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidStateTransitionException(
                    "Order " + po.getOrderNumber() + " is " + po.getStatus() + " and cannot be cancelled.");
        }
        inventoryService.releaseReservationsForOrder(po.getId());
        po.setStatus(OrderStatus.CANCELLED);
        po.setStatusReason(reason);
        productionOrderRepository.save(po);
        auditService.log("PRODUCTION_ORDER_CANCELLED", "ProductionOrder", String.valueOf(po.getId()), reason);
        return toDetail(po);
    }

    @Transactional
    public ProductionOrderDetailResponse reschedule(Long id, Instant plannedStart, Instant plannedEnd, String overrideReason) {
        ProductionOrder po = find(id);
        if (EnumSet.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED).contains(po.getStatus())) {
            throw new InvalidStateTransitionException("Completed or cancelled orders cannot be rescheduled.");
        }
        if (!plannedEnd.isAfter(plannedStart)) {
            throw new BadRequestException("INVALID_SCHEDULE", "Planned end must be after planned start.");
        }
        po.setPlannedStart(plannedStart);
        po.setPlannedEnd(plannedEnd);
        po.setScheduleOverrideReason(overrideReason);
        productionOrderRepository.save(po);
        return toDetail(po);
    }

    // ---------------------------------------------------------------- execution

    private static final Set<OrderStatus> REPORTABLE = EnumSet.of(OrderStatus.IN_PROGRESS, OrderStatus.PAUSED);

    @Transactional
    public ProductionOrderDetailResponse recordProduction(Long id, ProductionReportRequest req) {
        var existing = productionReportRepository.findByIdempotencyKey(req.getIdempotencyKey());
        ProductionOrder po = find(id);
        if (existing.isPresent()) {
            return toDetail(po);
        }
        if (!REPORTABLE.contains(po.getStatus())) {
            throw new InvalidStateTransitionException(
                    "Order " + po.getOrderNumber() + " is " + po.getStatus() + "; production can only be reported while in progress or paused.");
        }

        BigDecimal newCompleted = po.getCompletedQuantity().add(req.getCompletedQuantity());
        BigDecimal newRejected = po.getRejectedQuantity().add(req.getRejectedQuantity());
        if (newCompleted.add(newRejected).compareTo(po.getPlannedQuantity()) > 0) {
            throw new BadRequestException("OVER_REPORTING",
                    "Completed plus rejected quantity cannot exceed the planned quantity.");
        }

        ProductionReport report = new ProductionReport();
        report.setProductionOrderId(po.getId());
        report.setUserId(SecurityUtils.currentUserId().orElse(null));
        report.setCompletedQuantity(req.getCompletedQuantity());
        report.setRejectedQuantity(req.getRejectedQuantity());
        report.setDowntimeMinutes(req.getDowntimeMinutes());
        report.setNotes(req.getNotes());
        report.setIdempotencyKey(req.getIdempotencyKey());
        report = productionReportRepository.save(report);

        po.setCompletedQuantity(newCompleted);
        po.setRejectedQuantity(newRejected);
        productionOrderRepository.save(po);

        if (req.getCompletedQuantity().signum() > 0) {
            if (req.getLocationId() == null) {
                throw new BadRequestException("LOCATION_REQUIRED", "A storage location is required to record completed output.");
            }
            Inventory inventory = inventoryService.recordOutput(
                    po.getProductId(), po.getId(), po.getFactoryId(), req.getLocationId(), req.getCompletedQuantity());

            ProductionOutput output = new ProductionOutput();
            output.setProductionOrderId(po.getId());
            output.setProductId(po.getProductId());
            output.setBatchId(inventory.getBatchId());
            output.setReportId(report.getId());
            output.setFactoryId(po.getFactoryId());
            output.setLocationId(req.getLocationId());
            output.setQuantity(req.getCompletedQuantity());
            output.setStatus(OutputStatus.PENDING_INSPECTION);
            output = productionOutputRepository.save(output);

            notificationService.notifyRole(Role.QUALITY_INSPECTOR, "Output awaiting inspection",
                    req.getCompletedQuantity() + " units from order " + po.getOrderNumber()
                            + " are quarantined and pending quality inspection.",
                    NotificationType.INSPECTION_PENDING, "INSPECTION_PENDING:" + output.getId());
        }

        auditService.log("PRODUCTION_REPORTED", "ProductionOrder", String.valueOf(po.getId()),
                "Reported " + req.getCompletedQuantity() + " completed, " + req.getRejectedQuantity() + " rejected");
        return toDetail(po);
    }

    @Transactional
    public ProductionOrderDetailResponse complete(Long id) {
        ProductionOrder po = requireStatus(find(id), EnumSet.of(OrderStatus.IN_PROGRESS, OrderStatus.PAUSED));
        if (po.getCompletedQuantity().add(po.getRejectedQuantity()).signum() <= 0) {
            throw new BadRequestException("NOTHING_PRODUCED", "Cannot complete an order with no reported output.");
        }
        inventoryService.releaseReservationsForOrder(po.getId());
        po.setStatus(OrderStatus.COMPLETED);
        po.setActualEnd(Instant.now());
        productionOrderRepository.save(po);
        auditService.log("PRODUCTION_COMPLETED", "ProductionOrder", String.valueOf(po.getId()), "Production order completed");
        return toDetail(po);
    }

    /** Standalone single-material reservation (used by the inventory API) that keeps the item in sync. */
    @Transactional
    public List<MaterialReservation> reserveMaterialForOrder(Long productionOrderId, Long materialId,
                                                                BigDecimal quantity, Long factoryId, boolean allowPartial) {
        List<MaterialReservation> created =
                inventoryService.reserveMaterial(productionOrderId, materialId, quantity, factoryId, allowPartial);
        BigDecimal reserved = created.stream().map(MaterialReservation::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        productionOrderItemRepository.findByProductionOrderIdAndMaterialId(productionOrderId, materialId)
                .ifPresent(item -> {
                    item.setReservedQuantity(item.getReservedQuantity().add(reserved));
                    productionOrderItemRepository.save(item);
                });
        return created;
    }

    /**
     * Consumes material against this order's reservations and keeps the order's own
     * ProductionOrderItem.consumedQuantity in sync. This is the entry point the API
     * uses for consumption (see InventoryController) rather than calling
     * InventoryService directly, so that per-item progress is never missed.
     */
    @Transactional
    public List<MaterialConsumption> consumeMaterial(Long productionOrderId, Long materialId,
                                                        BigDecimal quantity, String idempotencyKey) {
        boolean replay = inventoryService.isConsumptionAlreadyApplied(idempotencyKey);
        List<MaterialConsumption> created =
                inventoryService.consume(productionOrderId, materialId, quantity, idempotencyKey);

        if (!replay) {
            productionOrderItemRepository.findByProductionOrderIdAndMaterialId(productionOrderId, materialId)
                    .ifPresent(item -> {
                        BigDecimal consumed = created.stream().map(MaterialConsumption::getQuantity)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        item.setConsumedQuantity(item.getConsumedQuantity().add(consumed));
                        productionOrderItemRepository.save(item);
                    });
        }
        return created;
    }

    // ---------------------------------------------------------------- helpers

    private ProductionOrder requireStatus(ProductionOrder po, Set<OrderStatus> allowed) {
        if (!allowed.contains(po.getStatus())) {
            throw new InvalidStateTransitionException(
                    "Order " + po.getOrderNumber() + " is " + po.getStatus() + "; expected one of " + allowed + ".");
        }
        return po;
    }

    private ProductionOrder find(Long id) {
        return productionOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Production order not found: " + id));
    }

    private ProductionOrderDetailResponse toDetail(ProductionOrder po) {
        ProductionOrderDetailResponse r = new ProductionOrderDetailResponse();
        r.setId(po.getId());
        r.setOrderNumber(po.getOrderNumber());
        r.setProductId(po.getProductId());
        r.setBomId(po.getBomId());
        r.setPlannedQuantity(po.getPlannedQuantity());
        r.setCompletedQuantity(po.getCompletedQuantity());
        r.setRejectedQuantity(po.getRejectedQuantity());
        r.setFactoryId(po.getFactoryId());
        r.setLineId(po.getLineId());
        r.setPriority(po.getPriority());
        r.setPlannedStart(po.getPlannedStart());
        r.setPlannedEnd(po.getPlannedEnd());
        r.setActualStart(po.getActualStart());
        r.setActualEnd(po.getActualEnd());
        r.setStatus(po.getStatus());
        r.setStatusBeforeHold(po.getStatusBeforeHold());
        r.setAllowPartialMaterials(Boolean.TRUE.equals(po.getAllowPartialMaterials()));
        r.setScheduleOverrideReason(po.getScheduleOverrideReason());
        r.setStatusReason(po.getStatusReason());
        r.setCreatedBy(po.getCreatedBy());
        r.setApprovedBy(po.getApprovedBy());
        r.setCreatedAt(po.getCreatedAt());
        r.setItems(productionOrderItemRepository.findByProductionOrderId(po.getId()).stream().map(item -> {
            ProductionOrderItemResponse ir = new ProductionOrderItemResponse();
            ir.setId(item.getId());
            ir.setProductionOrderId(item.getProductionOrderId());
            ir.setMaterialId(item.getMaterialId());
            ir.setRequiredQuantity(item.getRequiredQuantity());
            ir.setReservedQuantity(item.getReservedQuantity());
            ir.setConsumedQuantity(item.getConsumedQuantity());
            return ir;
        }).toList());
        return r;
    }
}
