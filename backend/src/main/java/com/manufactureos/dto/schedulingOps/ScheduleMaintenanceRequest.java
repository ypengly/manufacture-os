package com.manufactureos.dto.schedulingOps;

import com.manufactureos.entity.MaintenanceType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class ScheduleMaintenanceRequest {

    @NotNull
    private Long machineId;

    @NotNull
    private MaintenanceType maintenanceType;

    private String description;

    @NotNull
    private Instant scheduledStart;

    @NotNull
    private Instant scheduledEnd;

    private String technician;

    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }

    public MaintenanceType getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(MaintenanceType maintenanceType) { this.maintenanceType = maintenanceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(Instant scheduledStart) { this.scheduledStart = scheduledStart; }

    public Instant getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(Instant scheduledEnd) { this.scheduledEnd = scheduledEnd; }

    public String getTechnician() { return technician; }
    public void setTechnician(String technician) { this.technician = technician; }
}
