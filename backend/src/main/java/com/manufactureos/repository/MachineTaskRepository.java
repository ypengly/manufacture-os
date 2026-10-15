package com.manufactureos.repository;

import com.manufactureos.entity.MachineTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface MachineTaskRepository extends JpaRepository<MachineTask, Long> {

    List<MachineTask> findByMachineId(Long machineId);

    List<MachineTask> findByProductionOrderId(Long productionOrderId);

    /** Active (non-final) tasks for a machine whose window overlaps [start, end). */
    @Query("select t from MachineTask t where t.machineId = :machineId " +
           "and t.status in (com.manufactureos.entity.TaskStatus.SCHEDULED, com.manufactureos.entity.TaskStatus.READY, " +
           "com.manufactureos.entity.TaskStatus.RUNNING, com.manufactureos.entity.TaskStatus.PAUSED) " +
           "and t.startTime < :end and t.endTime > :start " +
           "and (:excludeId is null or t.id <> :excludeId)")
    List<MachineTask> findOverlapping(@Param("machineId") Long machineId, @Param("start") Instant start,
                                       @Param("end") Instant end, @Param("excludeId") Long excludeId);
}
