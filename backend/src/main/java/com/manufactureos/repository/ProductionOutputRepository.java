package com.manufactureos.repository;

import com.manufactureos.entity.ProductionOutput;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductionOutputRepository extends JpaRepository<ProductionOutput, Long> {
    List<ProductionOutput> findByProductionOrderId(Long productionOrderId);

    List<ProductionOutput> findByBatchId(Long batchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from ProductionOutput o where o.id = :id")
    Optional<ProductionOutput> lockById(@Param("id") Long id);
}
