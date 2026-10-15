package com.manufactureos.repository;

import com.manufactureos.entity.ReworkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ReworkOrderRepository extends JpaRepository<ReworkOrder, Long>, JpaSpecificationExecutor<ReworkOrder> {
    List<ReworkOrder> findByProductionOrderId(Long productionOrderId);
    List<ReworkOrder> findByOutputId(Long outputId);

    long countByStatus(com.manufactureos.entity.ReworkStatus status);
}
