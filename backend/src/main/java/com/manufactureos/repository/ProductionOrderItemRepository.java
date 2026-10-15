package com.manufactureos.repository;

import com.manufactureos.entity.ProductionOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductionOrderItemRepository extends JpaRepository<ProductionOrderItem, Long> {
    List<ProductionOrderItem> findByProductionOrderId(Long productionOrderId);
    Optional<ProductionOrderItem> findByProductionOrderIdAndMaterialId(Long productionOrderId, Long materialId);
}
