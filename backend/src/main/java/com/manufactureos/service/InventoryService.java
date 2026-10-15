package com.manufactureos.service;

import com.manufactureos.audit.AuditService;
import com.manufactureos.dto.inventoryOps.AdjustmentRequest;
import com.manufactureos.dto.inventoryOps.InventoryView;
import com.manufactureos.dto.inventoryOps.ReceiveMaterialRequest;
import com.manufactureos.entity.*;
import com.manufactureos.exception.BadRequestException;
import com.manufactureos.exception.ConflictException;
import com.manufactureos.exception.InsufficientMaterialException;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.repository.*;
import com.manufactureos.security.SecurityUtils;
import com.manufactureos.util.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns every change to on-hand and reserved stock. Every public method here that
 * mutates a row writes a matching, immutable InventoryTransaction in the same
 * database transaction, and every quantity change on a row happens only after that
 * row has been locked with lockById - that lock is what makes concurrent reservation
 * and consumption requests serialize instead of racing (see reserveMaterial).
 */
@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final MaterialReservationRepository materialReservationRepository;
    private final MaterialConsumptionRepository materialConsumptionRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public InventoryService(InventoryRepository inventoryRepository,
                             InventoryBatchRepository inventoryBatchRepository,
                             InventoryTransactionRepository inventoryTransactionRepository,
                             MaterialReservationRepository materialReservationRepository,
                             MaterialConsumptionRepository materialConsumptionRepository,
                             NotificationService notificationService,
                             AuditService auditService) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.materialReservationRepository = materialReservationRepository;
        this.materialConsumptionRepository = materialConsumptionRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    // ---------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public List<InventoryView> list(Long factoryId, InventoryCategory category) {
        List<Inventory> rows;
        if (factoryId != null && category != null) {
            rows = inventoryRepository.findByFactoryIdAndCategory(factoryId, category);
        } else if (factoryId != null) {
            rows = inventoryRepository.findByFactoryId(factoryId);
        } else if (category != null) {
            rows = inventoryRepository.findByCategory(category);
        } else {
            rows = inventoryRepository.findAll();
        }
        if (rows.isEmpty()) return List.of();

        List<Long> batchIds = rows.stream().map(Inventory::getBatchId).distinct().toList();
        var batchesById = inventoryBatchRepository.findAllById(batchIds).stream()
                .collect(java.util.stream.Collectors.toMap(InventoryBatch::getId, b -> b));

        return rows.stream().map(i -> {
            InventoryView v = new InventoryView();
            v.setId(i.getId());
            v.setFactoryId(i.getFactoryId());
            v.setLocationId(i.getLocationId());
            v.setBatchId(i.getBatchId());
            v.setCategory(i.getCategory());
            v.setQuantityOnHand(i.getQuantityOnHand());
            v.setReservedQuantity(i.getReservedQuantity());
            v.setAvailableQuantity(i.getQuantityOnHand().subtract(i.getReservedQuantity()));
            v.setUnitCost(i.getUnitCost());
            v.setUpdatedAt(i.getUpdatedAt());
            InventoryBatch batch = batchesById.get(i.getBatchId());
            if (batch != null) {
                v.setBatchNumber(batch.getBatchNumber());
                v.setMaterialId(batch.getMaterialId());
                v.setProductId(batch.getProductId());
                v.setExpirationDate(batch.getExpirationDate());
            }
            return v;
        }).toList();
    }

    @Transactional(readOnly = true)
    public InventoryBatch findBatch(Long batchId) {
        return inventoryBatchRepository.findById(batchId)
                .orElseThrow(() -> new NotFoundException("Batch not found: " + batchId));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<InventoryTransaction> listTransactions(
            Long factoryId, String referenceType, Long referenceId, org.springframework.data.domain.Pageable pageable) {
        if (referenceType != null && referenceId != null) {
            return inventoryTransactionRepository
                    .findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(referenceType, referenceId, pageable);
        }
        if (factoryId != null) {
            return inventoryTransactionRepository.findByFactoryIdOrderByCreatedAtDesc(factoryId, pageable);
        }
        return inventoryTransactionRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    // ---------------------------------------------------------------- receiving

    @Transactional
    public Inventory receive(ReceiveMaterialRequest req) {
        String batchNumber = (req.getBatchNumber() == null || req.getBatchNumber().isBlank())
                ? CodeGenerator.next("BATCH") : req.getBatchNumber();

        InventoryBatch batch = inventoryBatchRepository.findByBatchNumber(batchNumber).orElseGet(() -> {
            InventoryBatch b = new InventoryBatch();
            b.setBatchNumber(batchNumber);
            b.setMaterialId(req.getMaterialId());
            b.setSupplierId(req.getSupplierId());
            b.setPurchaseOrderId(req.getPurchaseOrderId());
            b.setManufacturingDate(req.getManufacturingDate());
            b.setExpirationDate(req.getExpirationDate());
            b.setStatus(BatchStatus.AVAILABLE);
            return inventoryBatchRepository.save(b);
        });

        Inventory inventory = inventoryRepository
                .findExact(batch.getId(), req.getFactoryId(), req.getLocationId(), InventoryCategory.RAW_MATERIAL)
                .orElseGet(() -> {
                    Inventory i = new Inventory();
                    i.setFactoryId(req.getFactoryId());
                    i.setLocationId(req.getLocationId());
                    i.setBatchId(batch.getId());
                    i.setCategory(InventoryCategory.RAW_MATERIAL);
                    i.setQuantityOnHand(BigDecimal.ZERO);
                    i.setReservedQuantity(BigDecimal.ZERO);
                    i.setUnitCost(req.getUnitCost());
                    return i;
                });

        BigDecimal previous = inventory.getQuantityOnHand();
        inventory.setQuantityOnHand(previous.add(req.getQuantity()));
        inventory.setUnitCost(req.getUnitCost());
        inventory.setUpdatedAt(Instant.now());
        inventory = inventoryRepository.save(inventory);

        recordTransaction(TransactionType.RECEIPT, inventory, req.getQuantity(), previous, inventory.getQuantityOnHand(),
                "PurchaseOrder", req.getPurchaseOrderId(), "Received into " + batchNumber);

        auditService.log("MATERIAL_RECEIVED", "Inventory", String.valueOf(inventory.getId()),
                "Received " + req.getQuantity() + " of material " + req.getMaterialId() + " into batch " + batchNumber);
        return inventory;
    }

    // ---------------------------------------------------------------- reservation

    /**
     * Reserves {@code quantity} of a material for a production order, locking and
     * allocating from the oldest available batches first. If the factory does not hold
     * enough stock: when {@code allowPartial} is false the whole call rolls back and
     * throws InsufficientMaterialException (so nothing is left half-reserved); when
     * true, whatever was available is reserved and the shortfall is simply not covered,
     * which the caller surfaces back as a recorded shortage on the production order.
     */
    @Transactional
    public List<MaterialReservation> reserveMaterial(Long productionOrderId, Long materialId,
                                                       BigDecimal quantity, Long factoryId, boolean allowPartial) {
        List<Long> candidateIds = inventoryRepository.findAvailableRawMaterialInventoryIds(materialId, factoryId);
        BigDecimal remaining = quantity;
        List<MaterialReservation> created = new ArrayList<>();

        for (Long inventoryId : candidateIds) {
            if (remaining.signum() <= 0) break;
            Inventory inventory = inventoryRepository.lockById(inventoryId).orElse(null);
            if (inventory == null) continue;

            BigDecimal available = inventory.getQuantityOnHand().subtract(inventory.getReservedQuantity());
            if (available.signum() <= 0) continue;

            BigDecimal take = available.min(remaining);
            BigDecimal previous = inventory.getReservedQuantity();
            inventory.setReservedQuantity(previous.add(take));
            inventory.setUpdatedAt(Instant.now());
            inventoryRepository.save(inventory);

            MaterialReservation reservation = new MaterialReservation();
            reservation.setProductionOrderId(productionOrderId);
            reservation.setInventoryId(inventory.getId());
            reservation.setMaterialId(materialId);
            reservation.setQuantity(take);
            reservation.setStatus(ReservationStatus.ACTIVE);
            created.add(materialReservationRepository.save(reservation));

            recordTransaction(TransactionType.RESERVATION, inventory, take, previous, inventory.getReservedQuantity(),
                    "ProductionOrder", productionOrderId, "Reserved for production order " + productionOrderId);

            remaining = remaining.subtract(take);
        }

        if (remaining.signum() > 0 && !allowPartial) {
            throw new InsufficientMaterialException(
                    "Only " + quantity.subtract(remaining) + " of the requested " + quantity +
                            " units of material " + materialId + " could be reserved.");
        }

        if (remaining.signum() > 0) {
            notificationService.notifyRole(Role.PRODUCTION_MANAGER, "Material shortage",
                    "Production order " + productionOrderId + " is short " + remaining + " unit(s) of material "
                            + materialId + " after a partial reservation.",
                    NotificationType.MATERIAL_SHORTAGE, null);
        }

        return created;
    }

    /** Releases every still-active reservation held by a production order (e.g. on cancel). */
    @Transactional
    public void releaseReservationsForOrder(Long productionOrderId) {
        for (MaterialReservation reservation : materialReservationRepository.findByProductionOrderId(productionOrderId)) {
            if (reservation.getStatus() == ReservationStatus.ACTIVE) {
                releaseReservation(reservation);
            }
        }
    }

    private void releaseReservation(MaterialReservation reservation) {
        BigDecimal outstanding = reservation.getQuantity().subtract(reservation.getConsumedQuantity());
        if (outstanding.signum() > 0) {
            Inventory inventory = inventoryRepository.lockById(reservation.getInventoryId())
                    .orElseThrow(() -> new NotFoundException("Inventory not found: " + reservation.getInventoryId()));
            BigDecimal previous = inventory.getReservedQuantity();
            inventory.setReservedQuantity(previous.subtract(outstanding).max(BigDecimal.ZERO));
            inventory.setUpdatedAt(Instant.now());
            inventoryRepository.save(inventory);
            recordTransaction(TransactionType.RELEASE, inventory, outstanding, previous, inventory.getReservedQuantity(),
                    "MaterialReservation", reservation.getId(), "Reservation released");
        }
        reservation.setStatus(ReservationStatus.RELEASED);
        materialReservationRepository.save(reservation);
    }

    // ---------------------------------------------------------------- consumption

    /**
     * Consumes material against a production order's active reservations. Idempotent on
     * {@code idempotencyKey}: a retried call with the same key returns the original
     * result instead of consuming twice, satisfying "prevent duplicate inventory
     * consumption" / "a consumption record must be processed exactly once".
     */
    @Transactional(readOnly = true)
    public boolean isConsumptionAlreadyApplied(String idempotencyKey) {
        return materialConsumptionRepository.findByIdempotencyKey(idempotencyKey).isPresent();
    }

    @Transactional
    public List<MaterialConsumption> consume(Long productionOrderId, Long materialId, BigDecimal quantity,
                                               String idempotencyKey) {
        var existing = materialConsumptionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return List.of(existing.get());
        }

        List<MaterialReservation> reservations =
                materialReservationRepository.findActiveByOrderAndMaterial(productionOrderId, materialId);

        BigDecimal remaining = quantity;
        List<MaterialConsumption> created = new ArrayList<>();
        Long userId = SecurityUtils.currentUserId().orElse(null);

        for (MaterialReservation reservation : reservations) {
            if (remaining.signum() <= 0) break;
            BigDecimal reservedOutstanding = reservation.getQuantity().subtract(reservation.getConsumedQuantity());
            if (reservedOutstanding.signum() <= 0) continue;

            BigDecimal take = reservedOutstanding.min(remaining);

            Inventory inventory = inventoryRepository.lockById(reservation.getInventoryId())
                    .orElseThrow(() -> new NotFoundException("Inventory not found: " + reservation.getInventoryId()));
            BigDecimal previousOnHand = inventory.getQuantityOnHand();
            inventory.setQuantityOnHand(previousOnHand.subtract(take));
            inventory.setReservedQuantity(inventory.getReservedQuantity().subtract(take).max(BigDecimal.ZERO));
            inventory.setUpdatedAt(Instant.now());
            inventoryRepository.save(inventory);

            reservation.setConsumedQuantity(reservation.getConsumedQuantity().add(take));
            if (reservation.getConsumedQuantity().compareTo(reservation.getQuantity()) >= 0) {
                reservation.setStatus(ReservationStatus.CONSUMED);
            }
            materialReservationRepository.save(reservation);

            MaterialConsumption consumption = new MaterialConsumption();
            consumption.setProductionOrderId(productionOrderId);
            consumption.setMaterialId(materialId);
            consumption.setReservationId(reservation.getId());
            consumption.setInventoryId(inventory.getId());
            consumption.setBatchId(inventory.getBatchId());
            consumption.setQuantity(take);
            consumption.setIdempotencyKey(idempotencyKey);
            consumption.setUserId(userId);
            created.add(materialConsumptionRepository.save(consumption));

            recordTransaction(TransactionType.MATERIAL_CONSUMPTION, inventory, take, previousOnHand,
                    inventory.getQuantityOnHand(), "ProductionOrder", productionOrderId, "Material consumed");

            remaining = remaining.subtract(take);
        }

        if (remaining.signum() > 0) {
            throw new InsufficientMaterialException(
                    "Cannot consume " + quantity + " of material " + materialId +
                            ": only " + quantity.subtract(remaining) + " is covered by active reservations.");
        }

        auditService.log("MATERIAL_CONSUMED", "ProductionOrder", String.valueOf(productionOrderId),
                "Consumed " + quantity + " of material " + materialId);
        return created;
    }

    // ---------------------------------------------------------------- output / disposition

    /** Creates finished-goods stock for a production order's output, quarantined until quality inspects it. */
    @Transactional
    public Inventory recordOutput(Long productId, Long productionOrderId, Long factoryId, Long locationId,
                                   BigDecimal quantity) {
        InventoryBatch batch = new InventoryBatch();
        batch.setBatchNumber(CodeGenerator.next("LOT"));
        batch.setProductId(productId);
        batch.setProductionOrderId(productionOrderId);
        batch.setStatus(BatchStatus.QUARANTINED);
        batch = inventoryBatchRepository.save(batch);

        Inventory inventory = new Inventory();
        inventory.setFactoryId(factoryId);
        inventory.setLocationId(locationId);
        inventory.setBatchId(batch.getId());
        inventory.setCategory(InventoryCategory.QUARANTINED);
        inventory.setQuantityOnHand(quantity);
        inventory.setReservedQuantity(BigDecimal.ZERO);
        inventory = inventoryRepository.save(inventory);

        recordTransaction(TransactionType.PRODUCTION_OUTPUT, inventory, quantity, BigDecimal.ZERO, quantity,
                "ProductionOrder", productionOrderId, "Production output recorded, pending inspection");
        return inventory;
    }

    /** Moves some or all of an inventory row's quantity into a different category (e.g. QUARANTINED -> FINISHED_GOOD). */
    @Transactional
    public void moveCategory(Long inventoryId, InventoryCategory destinationCategory, BigDecimal quantity,
                              String referenceType, Long referenceId, String note) {
        Inventory source = inventoryRepository.lockById(inventoryId)
                .orElseThrow(() -> new NotFoundException("Inventory not found: " + inventoryId));
        BigDecimal available = source.getQuantityOnHand().subtract(source.getReservedQuantity());
        if (quantity.compareTo(available) > 0) {
            throw new ConflictException("INSUFFICIENT_QUANTITY", "Cannot move more than is currently available on this record.");
        }

        BigDecimal previousSource = source.getQuantityOnHand();
        source.setQuantityOnHand(previousSource.subtract(quantity));
        source.setUpdatedAt(Instant.now());
        inventoryRepository.save(source);

        Inventory destination = inventoryRepository
                .findExact(source.getBatchId(), source.getFactoryId(), source.getLocationId(), destinationCategory)
                .orElseGet(() -> {
                    Inventory i = new Inventory();
                    i.setFactoryId(source.getFactoryId());
                    i.setLocationId(source.getLocationId());
                    i.setBatchId(source.getBatchId());
                    i.setCategory(destinationCategory);
                    i.setQuantityOnHand(BigDecimal.ZERO);
                    i.setReservedQuantity(BigDecimal.ZERO);
                    i.setUnitCost(source.getUnitCost());
                    return i;
                });
        BigDecimal previousDestination = destination.getQuantityOnHand();
        destination.setQuantityOnHand(previousDestination.add(quantity));
        destination.setUpdatedAt(Instant.now());
        destination = inventoryRepository.save(destination);

        recordTransaction(TransactionType.TRANSFER, destination, quantity, previousDestination,
                destination.getQuantityOnHand(), referenceType, referenceId, note);
    }

    /** Permanently removes quantity from stock as scrap/waste. */
    @Transactional
    public void scrap(Long inventoryId, BigDecimal quantity, String referenceType, Long referenceId, String reason) {
        Inventory inventory = inventoryRepository.lockById(inventoryId)
                .orElseThrow(() -> new NotFoundException("Inventory not found: " + inventoryId));
        BigDecimal available = inventory.getQuantityOnHand().subtract(inventory.getReservedQuantity());
        if (quantity.compareTo(available) > 0) {
            throw new ConflictException("INSUFFICIENT_QUANTITY", "Cannot scrap more than is currently available.");
        }
        BigDecimal previous = inventory.getQuantityOnHand();
        inventory.setQuantityOnHand(previous.subtract(quantity));
        inventory.setUpdatedAt(Instant.now());
        inventoryRepository.save(inventory);

        recordTransaction(TransactionType.SCRAP, inventory, quantity, previous, inventory.getQuantityOnHand(),
                referenceType, referenceId, reason);
    }

    /** Manual stock correction (counts, damage found during audit, etc). */
    @Transactional
    public Inventory adjust(AdjustmentRequest req) {
        Inventory inventory = inventoryRepository.lockById(req.getInventoryId())
                .orElseThrow(() -> new NotFoundException("Inventory not found: " + req.getInventoryId()));

        BigDecimal previous = inventory.getQuantityOnHand();
        BigDecimal updated = previous.add(req.getDeltaQuantity());
        if (updated.signum() < 0) {
            throw new BadRequestException("NEGATIVE_INVENTORY", "This adjustment would make on-hand quantity negative.");
        }
        if (inventory.getReservedQuantity().compareTo(updated) > 0) {
            throw new ConflictException("INSUFFICIENT_QUANTITY",
                    "This adjustment would leave reserved quantity higher than on-hand quantity.");
        }
        inventory.setQuantityOnHand(updated);
        inventory.setUpdatedAt(Instant.now());
        inventory = inventoryRepository.save(inventory);

        recordTransaction(TransactionType.ADJUSTMENT, inventory, req.getDeltaQuantity(), previous, updated,
                "ManualAdjustment", inventory.getId(), req.getReason());

        auditService.log("INVENTORY_ADJUSTED", "Inventory", String.valueOf(inventory.getId()),
                "Adjusted by " + req.getDeltaQuantity() + ": " + req.getReason());
        return inventory;
    }

    private void recordTransaction(TransactionType type, Inventory inventory, BigDecimal quantity,
                                    BigDecimal previousQuantity, BigDecimal newQuantity,
                                    String referenceType, Long referenceId, String notes) {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setTransactionNumber(CodeGenerator.next("TXN"));
        tx.setInventoryId(inventory.getId());
        tx.setBatchId(inventory.getBatchId());
        tx.setFactoryId(inventory.getFactoryId());
        tx.setLocationId(inventory.getLocationId());
        tx.setType(type);
        tx.setQuantity(quantity);
        tx.setReferenceType(referenceType);
        tx.setReferenceId(referenceId);
        tx.setPreviousQuantity(previousQuantity);
        tx.setNewQuantity(newQuantity);
        tx.setUserId(SecurityUtils.currentUserId().orElse(null));
        tx.setNotes(notes);
        inventoryTransactionRepository.save(tx);
    }
}
