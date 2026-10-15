package com.manufactureos.repository;

import com.manufactureos.entity.Bom;
import com.manufactureos.entity.BomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BomRepository extends JpaRepository<Bom, Long> {

    List<Bom> findByProductId(Long productId);

    List<Bom> findByProductIdAndStatus(Long productId, BomStatus status);

    @Query("select coalesce(max(b.version), 0) from Bom b where b.productId = :productId")
    Integer findMaxVersion(@Param("productId") Long productId);
}
