package com.manufactureos.repository;

import com.manufactureos.entity.QualityInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface QualityInspectionRepository extends JpaRepository<QualityInspection, Long>, JpaSpecificationExecutor<QualityInspection> {
    List<QualityInspection> findByProductionOrderId(Long productionOrderId);
    List<QualityInspection> findByOutputId(Long outputId);
    List<QualityInspection> findByBatchId(Long batchId);
    Optional<QualityInspection> findByInspectionNumber(String inspectionNumber);

    long countByFinalizedFalse();
}
