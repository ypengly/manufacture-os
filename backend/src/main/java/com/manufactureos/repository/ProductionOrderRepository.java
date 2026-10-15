package com.manufactureos.repository;

import com.manufactureos.entity.OrderStatus;
import com.manufactureos.entity.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
    List<ProductionOrder> findByStatus(OrderStatus status);
    List<ProductionOrder> findByFactoryId(Long factoryId);
    List<ProductionOrder> findByLineId(Long lineId);

    long countByStatus(OrderStatus status);
}
