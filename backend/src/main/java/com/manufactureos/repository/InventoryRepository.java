package com.manufactureos.repository;

import com.manufactureos.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Row-level lock on a single inventory row. Callers must already hold a transaction;
     * this blocks until any other transaction holding the same row's lock commits or rolls
     * back, which is exactly the serialization point that prevents two concurrent
     * reservations from both succeeding against the same stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.id = :id")
    Optional<Inventory> lockById(@Param("id") Long id);

    /** Candidate raw-material inventory rows with stock to spare, oldest batch first (FIFO). */
    @Query("select i.id from Inventory i, InventoryBatch b " +
           "where b.id = i.batchId and b.materialId = :materialId and i.factoryId = :factoryId " +
           "and i.category = com.manufactureos.entity.InventoryCategory.RAW_MATERIAL " +
           "and b.status = com.manufactureos.entity.BatchStatus.AVAILABLE " +
           "and (i.quantityOnHand - i.reservedQuantity) > 0 " +
           "order by b.expirationDate asc nulls last, i.id asc")
    List<Long> findAvailableRawMaterialInventoryIds(@Param("materialId") Long materialId,
                                                      @Param("factoryId") Long factoryId);

    @Query("select coalesce(sum(i.quantityOnHand - i.reservedQuantity), 0) from Inventory i, InventoryBatch b " +
           "where b.id = i.batchId and b.materialId = :materialId and i.factoryId = :factoryId " +
           "and i.category = com.manufactureos.entity.InventoryCategory.RAW_MATERIAL " +
           "and b.status = com.manufactureos.entity.BatchStatus.AVAILABLE")
    java.math.BigDecimal totalAvailableForMaterial(@Param("materialId") Long materialId,
                                                     @Param("factoryId") Long factoryId);

    /** Total available raw-material stock for a material across all factories (used for low-stock scans). */
    @Query("select coalesce(sum(i.quantityOnHand - i.reservedQuantity), 0) from Inventory i, InventoryBatch b " +
           "where b.id = i.batchId and b.materialId = :materialId " +
           "and i.category = com.manufactureos.entity.InventoryCategory.RAW_MATERIAL " +
           "and b.status = com.manufactureos.entity.BatchStatus.AVAILABLE")
    java.math.BigDecimal totalAvailableForMaterialAnyFactory(@Param("materialId") Long materialId);

    List<Inventory> findByBatchId(Long batchId);

    List<Inventory> findByFactoryId(Long factoryId);

    List<Inventory> findByFactoryIdAndCategory(Long factoryId, com.manufactureos.entity.InventoryCategory category);

    List<Inventory> findByCategory(com.manufactureos.entity.InventoryCategory category);

    @Query("select i from Inventory i where i.batchId = :batchId and i.factoryId = :factoryId " +
           "and i.locationId = :locationId and i.category = :category")
    Optional<Inventory> findExact(@Param("batchId") Long batchId, @Param("factoryId") Long factoryId,
                                   @Param("locationId") Long locationId,
                                   @Param("category") com.manufactureos.entity.InventoryCategory category);
}
