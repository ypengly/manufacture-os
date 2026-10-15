package com.manufactureos.dto.schedulingOps;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class RescheduleTaskRequest {

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
}
