package com.manufactureos.repository;

import com.manufactureos.entity.QualityDefect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface QualityDefectRepository extends JpaRepository<QualityDefect, Long>, JpaSpecificationExecutor<QualityDefect> {
    List<QualityDefect> findByProductionOrderId(Long productionOrderId);
    List<QualityDefect> findByBatchId(Long batchId);
    List<QualityDefect> findByStatus(com.manufactureos.entity.DefectStatus status);

    long countByStatus(com.manufactureos.entity.DefectStatus status);
}
