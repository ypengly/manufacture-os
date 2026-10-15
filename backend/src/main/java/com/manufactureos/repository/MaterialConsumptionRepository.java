package com.manufactureos.repository;

import com.manufactureos.entity.MaterialConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialConsumptionRepository extends JpaRepository<MaterialConsumption, Long> {
    Optional<MaterialConsumption> findByIdempotencyKey(String idempotencyKey);
    List<MaterialConsumption> findByProductionOrderId(Long productionOrderId);
}
