package com.manufactureos.dto.schedulingOps;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class ScheduleTaskRequest {

    @NotNull
    private Long machineId;

    @NotNull
    private Long productionOrderId;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }

    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
}
