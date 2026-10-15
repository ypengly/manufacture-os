package com.manufactureos.dto.machine;

import com.manufactureos.entity.MachineStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MachineRequest {

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 60)
    private String machineType;

    @NotNull
    private Long factoryId;

    private Long lineId;

    @DecimalMin(value = "0")
    private BigDecimal capacityPerHour;

    @Positive
    private Integer crewSize;

    @Size(max = 60)
    private String requiredSkill;

    @Positive
    private Integer maintenanceIntervalDays;

    private LocalDate lastMaintenanceDate;

    private LocalDate nextMaintenanceDate;

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
    public BigDecimal getCapacityPerHour() { return capacityPerHour; }
    public void setCapacityPerHour(BigDecimal capacityPerHour) { this.capacityPerHour = capacityPerHour; }
    public Integer getCrewSize() { return crewSize; }
    public void setCrewSize(Integer crewSize) { this.crewSize = crewSize; }
    public String getRequiredSkill() { return requiredSkill; }
    public void setRequiredSkill(String requiredSkill) { this.requiredSkill = requiredSkill; }
    public Integer getMaintenanceIntervalDays() { return maintenanceIntervalDays; }
    public void setMaintenanceIntervalDays(Integer maintenanceIntervalDays) { this.maintenanceIntervalDays = maintenanceIntervalDays; }
    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
    public LocalDate getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(LocalDate nextMaintenanceDate) { this.nextMaintenanceDate = nextMaintenanceDate; }
}
