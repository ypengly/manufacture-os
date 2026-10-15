package com.manufactureos.repository;

import com.manufactureos.entity.MachineMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface MachineMaintenanceRepository extends JpaRepository<MachineMaintenance, Long> {

    List<MachineMaintenance> findByMachineId(Long machineId);

    @Query("select m from MachineMaintenance m where m.machineId = :machineId " +
           "and m.status in (com.manufactureos.entity.MaintenanceStatus.SCHEDULED, com.manufactureos.entity.MaintenanceStatus.IN_PROGRESS) " +
           "and m.scheduledStart < :end and m.scheduledEnd > :start")
    List<MachineMaintenance> findOverlapping(@Param("machineId") Long machineId, @Param("start") Instant start,
                                              @Param("end") Instant end);

    List<MachineMaintenance> findByStatus(com.manufactureos.entity.MaintenanceStatus status);
}
