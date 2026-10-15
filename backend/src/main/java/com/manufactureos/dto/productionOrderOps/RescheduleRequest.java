package com.manufactureos.dto.productionOrderOps;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class RescheduleRequest {

    @NotNull
    private Instant plannedStart;

    @NotNull
    private Instant plannedEnd;

    /** Required only when the new window conflicts with another order on the same line. */
    private String overrideReason;

    public Instant getPlannedStart() { return plannedStart; }
    public void setPlannedStart(Instant plannedStart) { this.plannedStart = plannedStart; }
    public Instant getPlannedEnd() { return plannedEnd; }
    public void setPlannedEnd(Instant plannedEnd) { this.plannedEnd = plannedEnd; }
    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }
}
