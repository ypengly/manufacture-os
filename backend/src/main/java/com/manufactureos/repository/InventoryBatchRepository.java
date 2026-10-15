package com.manufactureos.repository;

import com.manufactureos.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
    Optional<InventoryBatch> findByBatchNumber(String batchNumber);
}
