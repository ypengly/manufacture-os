package com.manufactureos.repository;

import com.manufactureos.entity.MaterialReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MaterialReservationRepository extends JpaRepository<MaterialReservation, Long> {

    List<MaterialReservation> findByProductionOrderId(Long productionOrderId);

    @Query("select r from MaterialReservation r where r.productionOrderId = :poId and r.materialId = :materialId " +
           "and r.status = com.manufactureos.entity.ReservationStatus.ACTIVE order by r.id asc")
    List<MaterialReservation> findActiveByOrderAndMaterial(@Param("poId") Long productionOrderId,
                                                             @Param("materialId") Long materialId);
}
