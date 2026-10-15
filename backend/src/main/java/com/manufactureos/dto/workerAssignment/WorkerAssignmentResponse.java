package com.manufactureos.dto.workerAssignment;

import com.manufactureos.entity.AssignmentStatus;
import jakarta.validation.constraints.*;
import java.time.Instant;

public class WorkerAssignmentResponse {

    private Long id;

    private Long workerId;

    private Long machineTaskId;

    private Long productionOrderId;

    private Instant startTime;

    private Instant endTime;

    private AssignmentStatus status;

    private String reason;

    private Long createdBy;

    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public Long getMachineTaskId() { return machineTaskId; }
    public void setMachineTaskId(Long machineTaskId) { this.machineTaskId = machineTaskId; }
    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
