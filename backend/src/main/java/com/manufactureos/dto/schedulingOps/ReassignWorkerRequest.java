package com.manufactureos.dto.schedulingOps;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class ReassignWorkerRequest {

    @NotNull
    private Long newWorkerId;

    @NotBlank
    private String reason;

    private Instant startTime;

    private Instant endTime;

    public Long getNewWorkerId() { return newWorkerId; }
    public void setNewWorkerId(Long newWorkerId) { this.newWorkerId = newWorkerId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
}
