package com.manufactureos.repository;

import com.manufactureos.entity.WorkerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface WorkerAssignmentRepository extends JpaRepository<WorkerAssignment, Long> {

    List<WorkerAssignment> findByWorkerId(Long workerId);

    List<WorkerAssignment> findByMachineTaskId(Long machineTaskId);

    List<WorkerAssignment> findByProductionOrderId(Long productionOrderId);

    @Query("select a from WorkerAssignment a where a.workerId = :workerId " +
           "and a.status = com.manufactureos.entity.AssignmentStatus.ACTIVE " +
           "and a.startTime < :end and a.endTime > :start")
    List<WorkerAssignment> findOverlapping(@Param("workerId") Long workerId, @Param("start") Instant start,
                                            @Param("end") Instant end);
}
