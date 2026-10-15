package com.manufactureos.repository;

import com.manufactureos.entity.ScrapRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ScrapRecordRepository extends JpaRepository<ScrapRecord, Long>, JpaSpecificationExecutor<ScrapRecord> {
    List<ScrapRecord> findByProductionOrderId(Long productionOrderId);
}
