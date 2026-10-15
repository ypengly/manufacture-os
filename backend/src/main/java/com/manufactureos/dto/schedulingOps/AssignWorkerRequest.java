package com.manufactureos.dto.schedulingOps;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class AssignWorkerRequest {

    @NotNull
    private Long workerId;

    private Long machineTaskId;

    private Long productionOrderId;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

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
}
