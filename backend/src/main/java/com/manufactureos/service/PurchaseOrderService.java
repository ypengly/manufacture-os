package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.dto.inventoryOps.ReceiveMaterialRequest;
import com.manufactureos.dto.purchaseOrderItem.PurchaseOrderItemResponse;
import com.manufactureos.dto.purchaseOrderOps.PurchaseOrderCreateRequest;
import com.manufactureos.dto.purchaseOrderOps.PurchaseOrderDetailResponse;
import com.manufactureos.dto.purchaseOrderOps.PurchaseOrderItemInput;
import com.manufactureos.dto.purchaseOrderOps.ReceiveLineRequest;
import com.manufactureos.dto.purchaseOrderOps.ReceivePurchaseOrderRequest;
import com.manufactureos.entity.NotificationType;
import com.manufactureos.entity.PurchaseOrder;
import com.manufactureos.entity.PurchaseOrderItem;
import com.manufactureos.entity.PurchaseOrderStatus;
import com.manufactureos.entity.Role;
import com.manufactureos.exception.BadRequestException;
import com.manufactureos.exception.InvalidStateTransitionException;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.repository.PurchaseOrderItemRepository;
import com.manufactureos.repository.PurchaseOrderRepository;
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
 * Drives a purchase order from DRAFT through to (partially) RECEIVED, delegating the
 * actual stock movement for each receiving line to InventoryService.receive so that
 * every receipt still creates/updates a batch, an inventory row and an immutable
 * RECEIPT transaction the same way a non-PO receipt would.
 */
@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                 PurchaseOrderItemRepository purchaseOrderItemRepository,
                                 InventoryService inventoryService,
                                 NotificationService notificationService,
                                 AuditService auditService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public List<PurchaseOrderDetailResponse> list(PurchaseOrderStatus status, Long supplierId) {
        List<PurchaseOrder> orders;
        if (status != null) {
            orders = purchaseOrderRepository.findByStatus(status);
        } else if (supplierId != null) {
            orders = purchaseOrderRepository.findBySupplierId(supplierId);
        } else {
            orders = purchaseOrderRepository.findAll();
        }
        return orders.stream().map(this::toDetail).toList();
    }

    public PurchaseOrderDetailResponse get(Long id) {
        return toDetail(find(id));
    }

    @Transactional
    public PurchaseOrderDetailResponse create(PurchaseOrderCreateRequest req) {
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(CodeGenerator.next("PO"));
        po.setSupplierId(req.getSupplierId());
        po.setFactoryId(req.getFactoryId());
        po.setStatus(PurchaseOrderStatus.DRAFT);
        po.setExpectedDate(req.getExpectedDate());
        po.setCreatedBy(SecurityUtils.currentUserId().orElse(null));
        po.setCreatedAt(Instant.now());
        po = purchaseOrderRepository.save(po);

        for (PurchaseOrderItemInput input : req.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrderId(po.getId());
            item.setMaterialId(input.getMaterialId());
            item.setQuantity(input.getQuantity());
            item.setReceivedQuantity(BigDecimal.ZERO);
            item.setUnitCost(input.getUnitCost() != null ? input.getUnitCost() : BigDecimal.ZERO);
            purchaseOrderItemRepository.save(item);
        }

        auditService.log("PURCHASE_ORDER_CREATED", "PurchaseOrder", String.valueOf(po.getId()),
                "Created PO " + po.getPoNumber() + " with " + req.getItems().size() + " line(s)");
        return toDetail(po);
    }

    @Transactional
    public PurchaseOrderDetailResponse submit(Long id) {
        PurchaseOrder po = find(id);
        requireStatus(po, EnumSet.of(PurchaseOrderStatus.DRAFT));
        po.setStatus(PurchaseOrderStatus.SUBMITTED);

        notificationService.notifyRole(Role.ADMIN, "Purchase order awaiting approval",
                "PO " + po.getPoNumber() + " was submitted and needs approval.",
                NotificationType.ORDER_APPROVAL, "PO_SUBMITTED:" + po.getId());

        return toDetail(po);
    }

    @Transactional
    public PurchaseOrderDetailResponse approve(Long id) {
        PurchaseOrder po = find(id);
        requireStatus(po, EnumSet.of(PurchaseOrderStatus.SUBMITTED));
        po.setStatus(PurchaseOrderStatus.APPROVED);
        po.setApprovedBy(SecurityUtils.currentUserId().orElse(null));

        auditService.log("PURCHASE_ORDER_APPROVED", "PurchaseOrder", String.valueOf(id),
                "Approved PO " + po.getPoNumber());
        return toDetail(po);
    }

    @Transactional
    public PurchaseOrderDetailResponse cancel(Long id) {
        PurchaseOrder po = find(id);
        requireStatus(po, EnumSet.of(PurchaseOrderStatus.DRAFT, PurchaseOrderStatus.SUBMITTED, PurchaseOrderStatus.APPROVED));
        po.setStatus(PurchaseOrderStatus.CANCELLED);
        auditService.log("PURCHASE_ORDER_CANCELLED", "PurchaseOrder", String.valueOf(id), "Cancelled PO " + po.getPoNumber());
        return toDetail(po);
    }

    @Transactional
    public PurchaseOrderDetailResponse receive(Long id, ReceivePurchaseOrderRequest req) {
        PurchaseOrder po = purchaseOrderRepository.lockById(id)
                .orElseThrow(() -> new NotFoundException("Purchase order not found: " + id));
        requireStatus(po, EnumSet.of(PurchaseOrderStatus.APPROVED, PurchaseOrderStatus.PARTIALLY_RECEIVED));

        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(po.getId());

        for (ReceiveLineRequest line : req.getLines()) {
            PurchaseOrderItem item = items.stream()
                    .filter(i -> i.getId().equals(line.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("PO line not found: " + line.getItemId()));

            BigDecimal remaining = item.getQuantity().subtract(item.getReceivedQuantity());
            if (line.getQuantity().compareTo(remaining) > 0) {
                throw new BadRequestException("OVER_RECEIPT", "Cannot receive " + line.getQuantity()
                        + " against line " + item.getId() + "; only " + remaining + " remains outstanding");
            }

            ReceiveMaterialRequest receiveReq = new ReceiveMaterialRequest();
            receiveReq.setMaterialId(item.getMaterialId());
            receiveReq.setFactoryId(po.getFactoryId());
            receiveReq.setLocationId(line.getLocationId());
            receiveReq.setSupplierId(po.getSupplierId());
            receiveReq.setPurchaseOrderId(po.getId());
            receiveReq.setBatchNumber(line.getBatchNumber());
            receiveReq.setQuantity(line.getQuantity());
            receiveReq.setUnitCost(item.getUnitCost());
            receiveReq.setManufacturingDate(line.getManufacturingDate());
            receiveReq.setExpirationDate(line.getExpirationDate());
            inventoryService.receive(receiveReq);

            item.setReceivedQuantity(item.getReceivedQuantity().add(line.getQuantity()));
        }

        boolean allComplete = items.stream()
                .allMatch(i -> i.getReceivedQuantity().compareTo(i.getQuantity()) >= 0);
        po.setStatus(allComplete ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED);

        notificationService.notifyRole(Role.INVENTORY_MANAGER, "Purchase order received",
                "PO " + po.getPoNumber() + " is now " + po.getStatus() + ".",
                NotificationType.RECEIVING_EVENT, null);

        auditService.log("PURCHASE_ORDER_RECEIVED", "PurchaseOrder", String.valueOf(id),
                "Recorded receipt against PO " + po.getPoNumber() + "; status -> " + po.getStatus());
        return toDetail(po);
    }

    private void requireStatus(PurchaseOrder po, Set<PurchaseOrderStatus> allowed) {
        if (!allowed.contains(po.getStatus())) {
            throw new InvalidStateTransitionException(
                    "Purchase order " + po.getId() + " is " + po.getStatus() + "; expected one of " + allowed);
        }
    }

    private PurchaseOrder find(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase order not found: " + id));
    }

    private PurchaseOrderDetailResponse toDetail(PurchaseOrder po) {
        PurchaseOrderDetailResponse r = new PurchaseOrderDetailResponse();
        r.setId(po.getId());
        r.setPoNumber(po.getPoNumber());
        r.setSupplierId(po.getSupplierId());
        r.setFactoryId(po.getFactoryId());
        r.setStatus(po.getStatus());
        r.setExpectedDate(po.getExpectedDate());
        r.setCreatedBy(po.getCreatedBy());
        r.setApprovedBy(po.getApprovedBy());
        r.setCreatedAt(po.getCreatedAt());
        r.setVersion(po.getVersion());
        r.setItems(purchaseOrderItemRepository.findByPurchaseOrderId(po.getId()).stream()
                .map(this::toItemResponse).toList());
        return r;
    }

    private PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item) {
        PurchaseOrderItemResponse r = new PurchaseOrderItemResponse();
        r.setId(item.getId());
        r.setPurchaseOrderId(item.getPurchaseOrderId());
        r.setMaterialId(item.getMaterialId());
        r.setQuantity(item.getQuantity());
        r.setReceivedQuantity(item.getReceivedQuantity());
        r.setUnitCost(item.getUnitCost());
        return r;
    }
}
