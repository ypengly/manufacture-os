package com.manufactureos.dto.machine;

import com.manufactureos.entity.MachineStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MachineResponse {

    private Long id;

    private String code;

    private String name;

    private String machineType;

    private Long factoryId;

    private Long lineId;

    private MachineStatus status;

    private BigDecimal capacityPerHour;

    private Integer crewSize;

    private String requiredSkill;

    private BigDecimal operatingHours;

    private Integer maintenanceIntervalDays;

    private LocalDate lastMaintenanceDate;

    private LocalDate nextMaintenanceDate;

    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }
    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
    public Long getLineId() { return lineId; }
    public void setLineId(Long lineId) { this.lineId = lineId; }
    public MachineStatus getStatus() { return status; }
    public void setStatus(MachineStatus status) { this.status = status; }
    public BigDecimal getCapacityPerHour() { return capacityPerHour; }
    public void setCapacityPerHour(BigDecimal capacityPerHour) { this.capacityPerHour = capacityPerHour; }
    public Integer getCrewSize() { return crewSize; }
    public void setCrewSize(Integer crewSize) { this.crewSize = crewSize; }
    public String getRequiredSkill() { return requiredSkill; }
    public void setRequiredSkill(String requiredSkill) { this.requiredSkill = requiredSkill; }
    public BigDecimal getOperatingHours() { return operatingHours; }
    public void setOperatingHours(BigDecimal operatingHours) { this.operatingHours = operatingHours; }
    public Integer getMaintenanceIntervalDays() { return maintenanceIntervalDays; }
    public void setMaintenanceIntervalDays(Integer maintenanceIntervalDays) { this.maintenanceIntervalDays = maintenanceIntervalDays; }
    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
    public LocalDate getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(LocalDate nextMaintenanceDate) { this.nextMaintenanceDate = nextMaintenanceDate; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
