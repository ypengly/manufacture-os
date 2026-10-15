package com.manufactureos.repository;

import com.manufactureos.entity.QualityCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QualityCriterionRepository extends JpaRepository<QualityCriterion, Long>, JpaSpecificationExecutor<QualityCriterion> {
}
