package com.manufactureos.repository;

import com.manufactureos.entity.ProductionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductionReportRepository extends JpaRepository<ProductionReport, Long> {
    List<ProductionReport> findByProductionOrderId(Long productionOrderId);
    Optional<ProductionReport> findByIdempotencyKey(String idempotencyKey);
}
