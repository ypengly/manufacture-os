package com.manufactureos.repository;

import com.manufactureos.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    Page<InventoryTransaction> findByFactoryIdOrderByCreatedAtDesc(Long factoryId, Pageable pageable);
    Page<InventoryTransaction> findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(
            String referenceType, Long referenceId, Pageable pageable);
    Page<InventoryTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
